package com.liang.bbs.article.facade.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticleSearchDTO implements Serializable {
    /**
     * 文章编号
     */
    private Integer id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 标签编号
     */
    private List<Integer> labelIds;

    /**
     * 创建用户id
     */
    private Long createUser;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 搜索时间范围，可选值：day/week/month/year/older
     */
    private String timeRange;

    private static final long serialVersionUID = 1L;

}
