package com.liang.bbs.article.facade.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Elasticsearch availability snapshot for article search.
 */
@Data
public class ArticleSearchHealthDTO implements Serializable {
    private Boolean enabled;
    private Boolean available;
    private Boolean indexExists;
    private String articleIndex;
    private String message;

    private static final long serialVersionUID = 1L;
}
