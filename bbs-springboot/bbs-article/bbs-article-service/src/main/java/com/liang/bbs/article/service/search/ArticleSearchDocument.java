package com.liang.bbs.article.service.search;

import lombok.Data;

import java.util.List;

/**
 * Elasticsearch document model for article search.
 */
@Data
public class ArticleSearchDocument {
    private Integer articleId;
    private String title;
    private String content;
    private List<String> labelNames;
    private Integer state;
    private Boolean isDeleted;
    private Long createUser;
    private String createUserName;
    private String createTime;
}
