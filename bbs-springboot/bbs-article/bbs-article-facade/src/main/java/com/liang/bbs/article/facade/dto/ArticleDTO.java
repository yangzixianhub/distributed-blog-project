package com.liang.bbs.article.facade.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @date 2022/4/6 14:30
 */
@Data
public class ArticleDTO implements Serializable {
    private Integer id;

    private String titleMap;

    private String title;

    private String content;

    /**
     * Search highlight title returned by Elasticsearch.
     */
    private String highlightTitle;

    /**
     * Search highlight snippet returned by Elasticsearch.
     */
    private String highlightContent;

    private String markdown;

    private String html;

    private List<LabelDTO> labelDTOS;

    private Integer state;

    private Integer pv;

    private Integer top;

    private Boolean isDeleted;

    private Long createUser;

    private String createUserName;

    private String level;

    private Long updateUser;

    private String updateUserName;

    private ArticleCountDTO articleCountDTO;

    private String picture;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
