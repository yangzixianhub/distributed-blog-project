package com.liang.bbs.user.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikePo implements Serializable {
    //点赞编号
    private Long id;

    //文章id
    private Integer articleId;

    //状态
    private Boolean state;

    //点赞用户id
    private Long likeUser;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
