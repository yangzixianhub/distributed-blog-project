package com.liang.bbs.user.service.like;

public interface LikeStateRepository {
    boolean loadState(Integer targetId, Long userId);

    long loadCount(Integer targetId);

    void upsertState(Integer targetId, Long userId, boolean state);
}
