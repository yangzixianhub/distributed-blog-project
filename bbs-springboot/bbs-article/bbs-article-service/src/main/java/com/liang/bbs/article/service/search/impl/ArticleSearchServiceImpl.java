package com.liang.bbs.article.service.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.liang.bbs.article.facade.dto.ArticleDTO;
import com.liang.bbs.article.facade.dto.ArticleSearchDTO;
import com.liang.bbs.article.service.config.ElasticsearchProperties;
import com.liang.bbs.article.service.search.ArticleSearchDocument;
import com.liang.bbs.article.service.search.ArticleSearchPageResult;
import com.liang.bbs.article.service.search.ArticleSearchService;
import com.liang.bbs.common.enums.ArticleStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Minimal Elasticsearch search implementation for title search.
 */
@Slf4j
@Service
public class ArticleSearchServiceImpl implements ArticleSearchService {
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchProperties properties;

    public ArticleSearchServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchProperties properties) {
        this.elasticsearchClient = elasticsearchClient;
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {
        if (!isEnabled()) {
            return;
        }
        try {
            createIndexIfAbsent();
        } catch (Exception e) {
            log.warn("Elasticsearch index initialization skipped, index={}, reason={}",
                    properties.getArticleIndex(), e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    @Override
    public boolean supports(ArticleSearchDTO articleSearchDTO) {
        return isEnabled()
                && articleSearchDTO != null
                && StringUtils.isNotBlank(articleSearchDTO.getTitle())
                && articleSearchDTO.getId() == null
                && articleSearchDTO.getCreateUser() == null
                && CollectionUtils.isEmpty(articleSearchDTO.getLabelIds());
    }

    @Override
    public ArticleSearchPageResult search(ArticleSearchDTO articleSearchDTO, ArticleStateEnum articleStateEnum) {
        int currentPage = defaultCurrentPage(articleSearchDTO.getCurrentPage());
        int pageSize = defaultPageSize(articleSearchDTO.getPageSize());
        int from = (currentPage - 1) * pageSize;

        try {
            SearchResponse<ArticleSearchDocument> response = elasticsearchClient.search(search -> search
                            .index(properties.getArticleIndex())
                            .from(from)
                            .size(pageSize)
                            .query(query -> query.bool(bool -> {
                                bool.must(must -> must.multiMatch(multiMatch -> multiMatch
                                        .query(articleSearchDTO.getTitle())
                                        .fields("title^3", "content")));
                                bool.filter(filter -> filter.term(term -> term
                                        .field("isDeleted")
                                        .value(false)));
                                if (articleStateEnum != null) {
                                    bool.filter(filter -> filter.term(term -> term
                                            .field("state")
                                            .value(articleStateEnum.getCode())));
                                }
                                return bool;
                            })),
                    ArticleSearchDocument.class);

            ArticleSearchPageResult result = new ArticleSearchPageResult();
            result.setCurrentPage(currentPage);
            result.setPageSize(pageSize);
            result.setTotal(response.hits().total() == null ? 0L : response.hits().total().value());
            result.setArticleIds(extractArticleIds(response));
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch search failed", e);
        }
    }

    @Override
    public boolean save(ArticleDTO articleDTO) {
        if (!isEnabled() || articleDTO == null || articleDTO.getId() == null) {
            return false;
        }

        try {
            createIndexIfAbsent();
            elasticsearchClient.index(index -> index
                    .index(properties.getArticleIndex())
                    .id(String.valueOf(articleDTO.getId()))
                    .document(toDocument(articleDTO)));
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch save failed", e);
        }
    }

    @Override
    public boolean delete(Integer articleId) {
        if (!isEnabled() || articleId == null) {
            return false;
        }

        try {
            if (!indexExists()) {
                return false;
            }
            elasticsearchClient.delete(delete -> delete
                    .index(properties.getArticleIndex())
                    .id(String.valueOf(articleId)));
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch delete failed", e);
        }
    }

    @Override
    public int rebuild(List<ArticleDTO> articleDTOS) {
        if (!isEnabled()) {
            return 0;
        }

        List<ArticleDTO> safeArticles = articleDTOS == null ? Collections.emptyList() : articleDTOS.stream()
                .filter(Objects::nonNull)
                .filter(articleDTO -> articleDTO.getId() != null)
                .collect(Collectors.toList());

        try {
            resetIndex();
            if (safeArticles.isEmpty()) {
                return 0;
            }

            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
            safeArticles.forEach(articleDTO -> bulkBuilder.operations(operation -> operation
                    .index(index -> index
                            .index(properties.getArticleIndex())
                            .id(String.valueOf(articleDTO.getId()))
                            .document(toDocument(articleDTO)))));
            elasticsearchClient.bulk(bulkBuilder.build());
            return safeArticles.size();
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch rebuild failed", e);
        }
    }

    private void createIndexIfAbsent() throws IOException {
        if (indexExists()) {
            return;
        }

        elasticsearchClient.indices().create(create -> create
                .index(properties.getArticleIndex())
                .mappings(mappings -> mappings
                        .properties("articleId", Property.of(property -> property.integer(integer -> integer)))
                        .properties("title", Property.of(property -> property.text(text -> text
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart"))))
                        .properties("content", Property.of(property -> property.text(text -> text
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart"))))
                        .properties("state", Property.of(property -> property.integer(integer -> integer)))
                        .properties("isDeleted", Property.of(property -> property.boolean_(bool -> bool)))));
    }

    private boolean indexExists() throws IOException {
        return elasticsearchClient.indices()
                .exists(ExistsRequest.of(request -> request.index(properties.getArticleIndex())))
                .value();
    }

    private void resetIndex() throws IOException {
        if (indexExists()) {
            elasticsearchClient.indices().delete(delete -> delete.index(properties.getArticleIndex()));
        }
        createIndexIfAbsent();
    }

    private ArticleSearchDocument toDocument(ArticleDTO articleDTO) {
        ArticleSearchDocument document = new ArticleSearchDocument();
        document.setArticleId(articleDTO.getId());
        document.setTitle(articleDTO.getTitle());
        document.setContent(articleDTO.getContent());
        document.setState(articleDTO.getState());
        document.setIsDeleted(Boolean.TRUE.equals(articleDTO.getIsDeleted()));
        return document;
    }

    private List<Integer> extractArticleIds(SearchResponse<ArticleSearchDocument> response) {
        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(Objects::nonNull)
                .map(ArticleSearchDocument::getArticleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private int defaultCurrentPage(Integer currentPage) {
        return currentPage == null || currentPage <= 0 ? 1 : currentPage;
    }

    private int defaultPageSize(Integer pageSize) {
        return pageSize == null || pageSize <= 0 ? 10 : pageSize;
    }
}
