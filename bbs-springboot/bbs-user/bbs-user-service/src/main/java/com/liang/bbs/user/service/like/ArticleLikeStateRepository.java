package com.liang.bbs.user.service.like;

import com.liang.bbs.user.persistence.entity.LikePo;
import com.liang.bbs.user.persistence.entity.LikePoExample;
import com.liang.bbs.user.persistence.mapper.LikePoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ArticleLikeStateRepository implements LikeStateRepository {
    @Autowired
    private LikePoMapper likePoMapper;

    @Override
    public boolean loadState(Integer targetId, Long userId) {
        LikePoExample example = new LikePoExample();
        example.createCriteria().andArticleIdEqualTo(targetId).andLikeUserEqualTo(userId);
        List<LikePo> rows = likePoMapper.selectByExample(example);
        return !rows.isEmpty() && Boolean.TRUE.equals(rows.get(0).getState());
    }

    @Override
    public long loadCount(Integer targetId) {
        LikePoExample example = new LikePoExample();
        example.createCriteria().andArticleIdEqualTo(targetId).andStateEqualTo(true);
        return likePoMapper.countByExample(example);
    }

    @Override
    public void upsertState(Integer targetId, Long userId, boolean state) {
        LikePoExample example = new LikePoExample();
        example.createCriteria().andArticleIdEqualTo(targetId).andLikeUserEqualTo(userId);
        List<LikePo> rows = likePoMapper.selectByExample(example);
        LocalDateTime now = LocalDateTime.now();
        if (rows.isEmpty()) {
            LikePo row = new LikePo();
            row.setArticleId(targetId);
            row.setLikeUser(userId);
            row.setState(state);
            row.setCreateTime(now);
            row.setUpdateTime(now);
            likePoMapper.insertSelective(row);
            return;
        }
        LikePo row = rows.get(0);
        row.setState(state);
        row.setUpdateTime(now);
        likePoMapper.updateByPrimaryKeySelective(row);
    }
}
