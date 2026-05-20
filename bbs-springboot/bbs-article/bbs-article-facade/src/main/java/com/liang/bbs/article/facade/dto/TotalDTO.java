package com.liang.bbs.article.facade.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TotalDTO implements Serializable {

    /**
     * 文章数量
     */
    private Long articleCount;

    /**
     * 评论数量
     */
    private Long commentCount;

    /**
     * 访问数量
     */
    private Long visitCount;

    private static final long serialVersionUID = 1L;

}
