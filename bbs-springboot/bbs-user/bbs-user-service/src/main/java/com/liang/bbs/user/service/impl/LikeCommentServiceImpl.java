package com.liang.bbs.user.service.impl;

import com.liang.bbs.user.facade.dto.LikeCommentDTO;
import com.liang.bbs.user.facade.server.LikeCommentService;
import com.liang.bbs.user.persistence.entity.LikeCommentPo;
import com.liang.bbs.user.persistence.entity.LikeCommentPoExample;
import com.liang.bbs.user.persistence.mapper.LikeCommentPoExMapper;
import com.liang.bbs.user.persistence.mapper.LikeCommentPoMapper;
import com.liang.bbs.user.service.like.CommentLikeStateRepository;
import com.liang.bbs.user.service.like.LikeCacheCoordinator;
import com.liang.bbs.user.service.like.LikeTargetType;
import com.liang.bbs.user.service.mapstruct.LikeCommentMS;
import com.liang.nansheng.common.auth.UserSsoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Service
public class LikeCommentServiceImpl implements LikeCommentService {
    @Autowired
    private LikeCommentPoMapper likeCommentPoMapper;

    @Autowired
    private LikeCommentPoExMapper likeCommentPoExMapper;

    @Autowired
    private LikeCacheCoordinator likeCacheCoordinator;

    @Autowired
    private CommentLikeStateRepository commentLikeStateRepository;

    @Override
    public List<LikeCommentDTO> getPaasAll(LocalDateTime startTime, LocalDateTime endTime) {
        return LikeCommentMS.INSTANCE.toDTO(likeCommentPoExMapper.selectAllCommentLike(startTime, endTime));
    }

    @Override
    public Long getLikeCountCommentId(Integer commentId) {
        return likeCacheCoordinator.getCount(LikeTargetType.COMMENT, commentId, commentLikeStateRepository);
    }

    @Override
    public Boolean isLike(Integer commentId, Long userId) {
        return likeCacheCoordinator.isLiked(LikeTargetType.COMMENT, commentId, userId, commentLikeStateRepository);
    }

    @Override
    public Boolean updateLikeCommentState(Integer commentId, UserSsoDTO currentUser) {
        return likeCacheCoordinator.toggle(LikeTargetType.COMMENT, commentId, currentUser.getUserId(), commentLikeStateRepository);
    }

    @Override
    public LikeCommentDTO getByCommentIdUserId(Integer commentId, Long userId) {
        LikeCommentPoExample example = new LikeCommentPoExample();
        example.createCriteria().andCommentIdEqualTo(commentId).andLikeUserEqualTo(userId);
        List<LikeCommentDTO> likeCommentDTOS = LikeCommentMS.INSTANCE.toDTO(likeCommentPoMapper.selectByExample(example));
        return CollectionUtils.isEmpty(likeCommentDTOS) ? null : likeCommentDTOS.get(0);
    }
}
