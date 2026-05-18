package com.liang.bbs.article.service.search;

import com.liang.bbs.article.facade.dto.ArticleDTO;
import com.liang.bbs.article.facade.dto.ArticleLabelDTO;
import com.liang.bbs.article.facade.dto.ArticleMarkdownInfo;
import com.liang.bbs.article.facade.dto.LabelDTO;
import com.liang.bbs.article.persistence.entity.ArticlePo;
import com.liang.bbs.article.persistence.mapper.ArticlePoMapper;
import com.liang.bbs.article.service.config.ElasticsearchProperties;
import com.liang.bbs.article.service.mapstruct.ArticleMS;
import com.liang.bbs.article.facade.server.ArticleLabelService;
import com.liang.bbs.article.facade.server.LabelService;
import com.liang.bbs.common.enums.ArticleStateEnum;
import com.liang.manage.auth.facade.dto.user.UserDTO;
import com.liang.manage.auth.facade.server.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Async article index synchronization with simple retry/backoff semantics.
 */
@Slf4j
@Component
public class ArticleSearchSyncService {
    @Autowired
    private ArticlePoMapper articlePoMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ArticleLabelService articleLabelService;

    @Autowired
    private LabelService labelService;

    @DubboReference
    private UserService userService;

    @Autowired
    private ArticleSearchService articleSearchService;

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    @Async("articleSearchSyncExecutor")
    public void syncArticle(Integer articleId) {
        if (articleId == null || !articleSearchService.isEnabled()) {
            return;
        }

        executeWithRetry("sync", articleId, new SearchSyncAction() {
            @Override
            public boolean run() {
                ArticlePo articlePo = articlePoMapper.selectByPrimaryKey(articleId);
                if (!isSearchable(articlePo)) {
                    return articleSearchService.delete(articleId);
                }
                return articleSearchService.save(toIndexArticle(articlePo));
            }
        });
    }

    @Async("articleSearchSyncExecutor")
    public void deleteArticle(Integer articleId) {
        if (articleId == null || !articleSearchService.isEnabled()) {
            return;
        }

        executeWithRetry("delete", articleId, new SearchSyncAction() {
            @Override
            public boolean run() {
                return articleSearchService.delete(articleId);
            }
        });
    }

    private void executeWithRetry(String action, Integer articleId, SearchSyncAction searchSyncAction) {
        int retryCount = Math.max(1, elasticsearchProperties.getSyncRetryCount());
        long backoffMillis = Math.max(0L, elasticsearchProperties.getSyncRetryBackoffMillis());
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                if (searchSyncAction.run()) {
                    return;
                }
            } catch (Exception e) {
                log.warn("Article search {} failed, articleId={}, attempt={}/{}",
                        action, articleId, attempt, retryCount, e);
            }

            if (attempt < retryCount && backoffMillis > 0L) {
                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        log.error("Article search {} exhausted retries, articleId={}, retryCount={}", action, articleId, retryCount);
    }

    private ArticleDTO toIndexArticle(ArticlePo articlePo) {
        ArticleDTO articleDTO = ArticleMS.INSTANCE.toDTO(articlePo);
        attachSearchContent(articleDTO);
        attachLabels(articleDTO);
        attachAuthor(articleDTO);
        return articleDTO;
    }

    private void attachSearchContent(ArticleDTO articleDTO) {
        if (articleDTO == null || articleDTO.getId() == null) {
            return;
        }

        List<ArticleMarkdownInfo> articleMarkdownInfos = getMarkdownByArticleIds(Collections.singletonList(articleDTO.getId()));
        if (CollectionUtils.isEmpty(articleMarkdownInfos)) {
            return;
        }

        articleDTO.setContent(extractSearchContent(articleMarkdownInfos.get(0), articleDTO.getContent()));
    }

    private void attachLabels(ArticleDTO articleDTO) {
        if (articleDTO == null || articleDTO.getId() == null) {
            return;
        }

        List<ArticleLabelDTO> articleLabelDTOS = articleLabelService.getByArticleIds(Collections.singletonList(articleDTO.getId()));
        if (CollectionUtils.isEmpty(articleLabelDTOS)) {
            return;
        }

        List<Integer> labelIds = articleLabelDTOS.stream()
                .map(ArticleLabelDTO::getLabelId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(labelIds)) {
            return;
        }

        articleDTO.setLabelDTOS(labelService.getByIds(labelIds));
    }

    private void attachAuthor(ArticleDTO articleDTO) {
        if (articleDTO == null || articleDTO.getCreateUser() == null) {
            return;
        }

        Map<Long, List<UserDTO>> userMap = userService.getByIds(Collections.singletonList(articleDTO.getCreateUser()))
                .stream()
                .collect(Collectors.groupingBy(UserDTO::getId));
        List<UserDTO> userDTOS = userMap.get(articleDTO.getCreateUser());
        if (CollectionUtils.isNotEmpty(userDTOS)) {
            articleDTO.setCreateUserName(userDTOS.get(0).getName());
        }
    }

    private List<ArticleMarkdownInfo> getMarkdownByArticleIds(List<Integer> articleIds) {
        Query query = new Query(Criteria.where("articleId").in(articleIds));
        return mongoTemplate.find(query, ArticleMarkdownInfo.class);
    }

    private String extractSearchContent(ArticleMarkdownInfo articleMarkdownInfo, String fallbackContent) {
        if (articleMarkdownInfo == null) {
            return fallbackContent;
        }
        if (StringUtils.isNotBlank(articleMarkdownInfo.getArticleHtml())) {
            String content = com.liang.nansheng.common.utils.CommonUtils.html2Text(articleMarkdownInfo.getArticleHtml());
            if (StringUtils.isNotBlank(content)) {
                return content;
            }
        }
        if (StringUtils.isNotBlank(articleMarkdownInfo.getArticleMarkdown())) {
            return articleMarkdownInfo.getArticleMarkdown();
        }
        return fallbackContent;
    }

    private boolean isSearchable(ArticlePo articlePo) {
        return articlePo != null
                && !Boolean.TRUE.equals(articlePo.getIsDeleted())
                && ArticleStateEnum.enable.getCode().equals(articlePo.getState());
    }

    private interface SearchSyncAction {
        boolean run();
    }
}
