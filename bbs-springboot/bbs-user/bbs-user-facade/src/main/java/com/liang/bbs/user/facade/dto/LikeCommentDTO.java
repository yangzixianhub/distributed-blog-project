package com.liang.bbs.user.facade.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class LikeCommentDTO implements Serializable {
    //评论点赞编号
    private Long id;

    //评论id
    private Long commentId;

    //状态
    private Boolean state;

    //评论点赞用户id
    private Long likeUser;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;

}
