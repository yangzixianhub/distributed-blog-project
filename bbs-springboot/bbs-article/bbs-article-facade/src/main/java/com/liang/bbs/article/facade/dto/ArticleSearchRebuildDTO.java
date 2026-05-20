package com.liang.bbs.article.facade.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Article search index rebuild result.
 */
@Data
public class ArticleSearchRebuildDTO implements Serializable {
    private Integer indexedCount;
    private Long costMillis;

    private static final long serialVersionUID = 1L;
}
