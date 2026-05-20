package com.liang.bbs.article.facade.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class CommentDTO implements Serializable {
    //评论编号
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    //父评论id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long preId;

    //评论内容
    private String content;

    //被评论帖子id
    private Integer articleId;

    //状态
    private Boolean state;

    //逻辑删除
    private Boolean isDeleted;

    //评论用户id
    private Long commentUser;

    //评论用户名称
    private String commentUserName;

    //用户头像
    private String picture;

    //等级
    private String level;

    //是否点赞
    private Boolean isLike;

    //点赞数量
    private Long likeCount;

    //回复数量
    private Integer repliesCount;

    //评论深度
    private Integer depth;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

    private List<CommentDTO> child;

    private static final long serialVersionUID = 1L;

}
