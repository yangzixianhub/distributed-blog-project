package com.liang.bbs.user.service.like;

import com.liang.bbs.user.persistence.entity.LikeCommentPo;
import com.liang.bbs.user.persistence.entity.LikeCommentPoExample;
import com.liang.bbs.user.persistence.mapper.LikeCommentPoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CommentLikeStateRepository implements LikeStateRepository {
    @Autowired
    private LikeCommentPoMapper likeCommentPoMapper;

    @Override
    public boolean loadState(Integer targetId, Long userId) {
        LikeCommentPoExample example = new LikeCommentPoExample();
        example.createCriteria().andCommentIdEqualTo(targetId).andLikeUserEqualTo(userId);
        List<LikeCommentPo> rows = likeCommentPoMapper.selectByExample(example);
        return !rows.isEmpty() && Boolean.TRUE.equals(rows.get(0).getState());
    }

    @Override
    public long loadCount(Integer targetId) {
        LikeCommentPoExample example = new LikeCommentPoExample();
        example.createCriteria().andCommentIdEqualTo(targetId).andStateEqualTo(true);
        return likeCommentPoMapper.countByExample(example);
    }

    @Override
    public void upsertState(Integer targetId, Long userId, boolean state) {
        LikeCommentPoExample example = new LikeCommentPoExample();
        example.createCriteria().andCommentIdEqualTo(targetId).andLikeUserEqualTo(userId);
        List<LikeCommentPo> rows = likeCommentPoMapper.selectByExample(example);
        LocalDateTime now = LocalDateTime.now();
        if (rows.isEmpty()) {
            LikeCommentPo row = new LikeCommentPo();
            row.setCommentId(targetId);
            row.setLikeUser(userId);
            row.setState(state);
            row.setCreateTime(now);
            row.setUpdateTime(now);
            likeCommentPoMapper.insertSelective(row);
            return;
        }
        LikeCommentPo row = rows.get(0);
        row.setState(state);
        row.setUpdateTime(now);
        likeCommentPoMapper.updateByPrimaryKeySelective(row);
    }
}
