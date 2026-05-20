package com.liang.bbs.user.service.like;

import com.liang.bbs.common.distributedid.SnowflakeIdScope;
import com.liang.bbs.common.distributedid.SnowflakeIdService;
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

    @Autowired
    private SnowflakeIdService snowflakeIdService;

    private static Integer toArticleId(Long targetId) {
        return targetId.intValue();
    }

    @Override
    public boolean loadState(Long targetId, Long userId) {
        LikePoExample example = new LikePoExample();
        example.createCriteria().andArticleIdEqualTo(toArticleId(targetId)).andLikeUserEqualTo(userId);
        List<LikePo> rows = likePoMapper.selectByExample(example);
        return !rows.isEmpty() && Boolean.TRUE.equals(rows.get(0).getState());
    }

    @Override
    public long loadCount(Long targetId) {
        LikePoExample example = new LikePoExample();
        example.createCriteria().andArticleIdEqualTo(toArticleId(targetId)).andStateEqualTo(true);
        return likePoMapper.countByExample(example);
    }

    @Override
    public void upsertState(Long targetId, Long userId, boolean state) {
        LikePoExample example = new LikePoExample();
        example.createCriteria().andArticleIdEqualTo(toArticleId(targetId)).andLikeUserEqualTo(userId);
        List<LikePo> rows = likePoMapper.selectByExample(example);
        LocalDateTime now = LocalDateTime.now();
        if (rows.isEmpty()) {
            LikePo row = new LikePo();
            row.setArticleId(toArticleId(targetId));
            row.setLikeUser(userId);
            row.setState(state);
            row.setCreateTime(now);
            row.setUpdateTime(now);
            snowflakeIdService.assignPrimaryKey(row::setId, SnowflakeIdScope.LIKE);
            likePoMapper.insertSelective(row);
            return;
        }
        LikePo row = rows.get(0);
        row.setState(state);
        row.setUpdateTime(now);
        likePoMapper.updateByPrimaryKeySelective(row);
    }
}
