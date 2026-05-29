package com.liang.bbs.article.facade.dto;

import lombok.Data;

import java.io.Serializable;

//文章静态HTML批量重建结果
@Data
public class ArticleStaticHtmlRebuildDTO implements Serializable {
    private Integer publishedCount;
    private Long costMillis;

    private static final long serialVersionUID = 1L;
}
