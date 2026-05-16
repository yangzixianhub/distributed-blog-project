package com.liang.bbs.article.service.search;

import lombok.Data;

/**
 * Elasticsearch document model for article search.
 */
@Data
public class ArticleSearchDocument {
    private Integer articleId;
    private String title;
    private String content;
    private Integer state;
    private Boolean isDeleted;
}
