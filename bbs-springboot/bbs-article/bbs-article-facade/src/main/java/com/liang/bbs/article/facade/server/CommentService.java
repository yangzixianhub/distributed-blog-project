package com.liang.bbs.article.facade.server;

import com.github.pagehelper.PageInfo;
import com.liang.bbs.article.facade.dto.CommentDTO;
import com.liang.bbs.article.facade.dto.CommentSearchDTO;
import com.liang.nansheng.common.auth.UserSsoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    /**
     * 获取文章的评论信息
     *
     * @param commentSearchDTO
     * @return
     */
    List<CommentDTO> getCommentByArticleId(CommentSearchDTO commentSearchDTO, UserSsoDTO currentUser);

    /**
     * 获取所有通过审核文章的评论信息
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<CommentDTO> getAllArticleComment(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取所有通过审核文章的评论回复信息
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<CommentDTO> getAllCommentReply(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取最新评论信息
     *
     * @param commentSearchDTO
     * @return
     */
    PageInfo<CommentDTO> getLatestComment(CommentSearchDTO commentSearchDTO);

    /**
     * 获取文章的评论数量
     *
     * @param articleId
     * @return
     */
    Long getCommentCountByArticle(Integer articleId);

    /**
     * 获取评论数量
     *
     * @return
     */
    Long getTotal();

    /**
     * 创建评论
     *
     * @param commentDTO
     * @param currentUser
     * @return
     */
    Boolean create(CommentDTO commentDTO, UserSsoDTO currentUser);

    /**
     * 删除评论
     *
     * @param commentId
     * @return
     */
    Boolean delete(Long commentId);

    /**
     * 通过父级ID获取子级评论信息
     *
     * @param result 存放结果
     * @param preId
     * @return
     */
    void getAllChildrenByPreId(List<CommentDTO> result, Long preId);

    /**
     * 获取评论id获取文章id
     *
     * @param commentId
     * @return
     */
    Integer getArticleIdByCommentId(Long commentId);

    /**
     * 通过批量id获取评论信息
     *
     * @param commentId
     * @return
     */
    CommentDTO getById(Long commentId);
}
