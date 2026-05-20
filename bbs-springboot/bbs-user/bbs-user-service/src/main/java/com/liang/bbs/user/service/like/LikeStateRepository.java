package com.liang.bbs.user.service.like;

public interface LikeStateRepository {
    boolean loadState(Long targetId, Long userId);

    long loadCount(Long targetId);

    void upsertState(Long targetId, Long userId, boolean state);
}
