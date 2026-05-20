package com.liang.bbs.article.service.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.liang.bbs.article.facade.dto.ArticleDTO;
import com.liang.bbs.article.facade.dto.ArticleSearchHealthDTO;
import com.liang.bbs.article.facade.dto.ArticleSearchDTO;
import com.liang.bbs.article.facade.dto.LabelDTO;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Elasticsearch search implementation for article title/content search.
 */
@Slf4j
@Service
public class ArticleSearchServiceImpl implements ArticleSearchService {
    private static final String TITLE_FIELD = "title";
    private static final String CONTENT_FIELD = "content";
    private static final String LABEL_FIELD = "labelNames";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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
                && (StringUtils.isNotBlank(articleSearchDTO.getTitle())
                || StringUtils.isNotBlank(articleSearchDTO.getTimeRange()))
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
                                if (StringUtils.isNotBlank(articleSearchDTO.getTitle())) {
                                    bool.must(must -> must.multiMatch(multiMatch -> multiMatch
                                            .query(articleSearchDTO.getTitle())
                                            .fields("title^4", "content", "labelNames^2", "createUserName^1.5")));
                                }
                                bool.filter(filter -> filter.term(term -> term
                                        .field("isDeleted")
                                        .value(false)));
                                if (articleStateEnum != null) {
                                    bool.filter(filter -> filter.term(term -> term
                                            .field("state")
                                            .value(articleStateEnum.getCode())));
                                }
                                LocalDateTime startTime = resolveStartTime(articleSearchDTO.getTimeRange());
                                LocalDateTime endTime = resolveEndTime(articleSearchDTO.getTimeRange());
                                if (startTime != null || endTime != null) {
                                    bool.filter(filter -> filter.range(range -> {
                                        range.field("createTime");
                                        if (startTime != null) {
                                            range.gte(JsonData.of(startTime.format(DATE_TIME_FORMATTER)));
                                        }
                                        if (endTime != null) {
                                            range.lt(JsonData.of(endTime.format(DATE_TIME_FORMATTER)));
                                        }
                                        return range;
                                    }));
                                }
                                return bool;
                            }))
                            .sort(sort -> {
                                if (StringUtils.isNotBlank(articleSearchDTO.getTitle())) {
                                    return sort.score(score -> score.order(co.elastic.clients.elasticsearch._types.SortOrder.Desc));
                                }
                                return sort.field(field -> field
                                        .field("createTime")
                                        .order(co.elastic.clients.elasticsearch._types.SortOrder.Desc));
                            })
                            .sort(sort -> sort.field(field -> field
                                    .field("createTime")
                                    .order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                            .highlight(highlight -> highlight
                                    .preTags("<em class='search-highlight'>")
                                    .postTags("</em>")
                                    .fields(TITLE_FIELD, field -> field.numberOfFragments(0))
                                    .fields(LABEL_FIELD, field -> field.numberOfFragments(0))
                                    .fields(CONTENT_FIELD, field -> field
                                            .fragmentSize(120)
                                            .numberOfFragments(1))),
                    ArticleSearchDocument.class);

            ArticleSearchPageResult result = new ArticleSearchPageResult();
            result.setCurrentPage(currentPage);
            result.setPageSize(pageSize);
            result.setTotal(response.hits().total() == null ? 0L : response.hits().total().value());
            result.setArticleIds(extractArticleIds(response));
            fillHighlightMaps(response, result);
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
            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkBuilder.build());
            if (bulkResponse.errors()) {
                String errorMessage = bulkResponse.items().stream()
                        .filter(item -> item.error() != null)
                        .map(item -> item.id() + ":" + item.error().reason())
                        .findFirst()
                        .orElse("unknown bulk error");
                throw new IllegalStateException("Elasticsearch rebuild bulk item failed: " + errorMessage);
            }
            return safeArticles.size();
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch rebuild failed", e);
        }
    }

    @Override
    public ArticleSearchHealthDTO health() {
        ArticleSearchHealthDTO result = new ArticleSearchHealthDTO();
        result.setEnabled(properties.isEnabled());
        result.setArticleIndex(properties.getArticleIndex());
        if (!properties.isEnabled()) {
            result.setAvailable(false);
            result.setIndexExists(false);
            result.setMessage("Elasticsearch is disabled");
            return result;
        }

        try {
            BooleanResponse pingResponse = elasticsearchClient.ping();
            boolean available = pingResponse.value();
            boolean indexExists = available && indexExists();
            result.setAvailable(available);
            result.setIndexExists(indexExists);
            result.setMessage(available ? "ok" : "ping failed");
            return result;
        } catch (Exception e) {
            result.setAvailable(false);
            result.setIndexExists(false);
            result.setMessage(e.getMessage());
            return result;
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
                        .properties(TITLE_FIELD, Property.of(property -> property.text(text -> text
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart"))))
                        .properties(CONTENT_FIELD, Property.of(property -> property.text(text -> text
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart"))))
                        .properties(LABEL_FIELD, Property.of(property -> property.text(text -> text
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart"))))
                        .properties("state", Property.of(property -> property.integer(integer -> integer)))
                        .properties("isDeleted", Property.of(property -> property.boolean_(bool -> bool)))
                        .properties("createUser", Property.of(property -> property.long_(longProperty -> longProperty)))
                        .properties("createUserName", Property.of(property -> property.text(text -> text
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart"))))
                        .properties("createTime", Property.of(property -> property.date(date -> date)))));
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
        document.setLabelNames(extractLabelNames(articleDTO.getLabelDTOS()));
        document.setState(articleDTO.getState());
        document.setIsDeleted(Boolean.TRUE.equals(articleDTO.getIsDeleted()));
        document.setCreateUser(articleDTO.getCreateUser());
        document.setCreateUserName(articleDTO.getCreateUserName());
        document.setCreateTime(articleDTO.getCreateTime() == null ? null : articleDTO.getCreateTime().format(DATE_TIME_FORMATTER));
        return document;
    }

    private List<Integer> extractArticleIds(SearchResponse<ArticleSearchDocument> response) {
        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(ArticleSearchDocument::getArticleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void fillHighlightMaps(SearchResponse<ArticleSearchDocument> response, ArticleSearchPageResult result) {
        for (Hit<ArticleSearchDocument> hit : response.hits().hits()) {
            ArticleSearchDocument source = hit.source();
            if (source == null || source.getArticleId() == null || hit.highlight() == null) {
                continue;
            }

            Integer articleId = source.getArticleId();
            putFirstHighlight(hit.highlight(), TITLE_FIELD, result.getHighlightTitleMap(), articleId);
            putFirstHighlight(hit.highlight(), CONTENT_FIELD, result.getHighlightContentMap(), articleId);
            if (!result.getHighlightTitleMap().containsKey(articleId)) {
                putFirstHighlight(hit.highlight(), LABEL_FIELD, result.getHighlightTitleMap(), articleId);
            }
        }
    }

    private List<String> extractLabelNames(List<LabelDTO> labelDTOS) {
        if (CollectionUtils.isEmpty(labelDTOS)) {
            return new ArrayList<>();
        }
        return labelDTOS.stream()
                .map(LabelDTO::getLabelName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private void putFirstHighlight(Map<String, List<String>> highlightMap, String field,
                                   Map<Integer, String> targetMap, Integer articleId) {
        List<String> fragments = highlightMap.get(field);
        if (CollectionUtils.isNotEmpty(fragments) && StringUtils.isNotBlank(fragments.get(0))) {
            targetMap.put(articleId, fragments.get(0));
        }
    }

    private int defaultCurrentPage(Integer currentPage) {
        return currentPage == null || currentPage <= 0 ? 1 : currentPage;
    }

    private int defaultPageSize(Integer pageSize) {
        return pageSize == null || pageSize <= 0 ? 10 : pageSize;
    }

    private LocalDateTime resolveStartTime(String timeRange) {
        if (StringUtils.isBlank(timeRange)) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        switch (timeRange) {
            case "day":
                return now.minusDays(1);
            case "week":
                return now.minusWeeks(1);
            case "month":
                return now.minusMonths(1);
            case "year":
                return now.minusYears(1);
            default:
                return null;
        }
    }

    private LocalDateTime resolveEndTime(String timeRange) {
        if (!"older".equals(timeRange)) {
            return null;
        }
        return LocalDateTime.now().minusYears(1);
    }
}
