package com.liang.bbs.user.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DynamicPo implements Serializable {
    //用户动态编号
    private Integer id;

    //类型（写文章、评论、点赞、关注等）
    private String type;

    //发起人
    private Long userId;

    //操作的对象ID（文章id、用户id等）
    private String objectId;

    //评论id
    private Long commentId;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
