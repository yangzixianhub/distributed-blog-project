package com.liang.bbs.user.service.like;

import com.liang.bbs.common.constant.RedisConstants;

public enum LikeTargetType {
    ARTICLE(RedisConstants.ARTICLE_LIKE_KEY),
    COMMENT(RedisConstants.COMMENT_LIKE_KEY);

    private final String keyPrefix;

    LikeTargetType(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String countKey(Long targetId) {
        return keyPrefix + "count:" + targetId;
    }

    public String stateKey(Long targetId) {
        return keyPrefix + "state:" + targetId;
    }

    public String dirtyKey() {
        return keyPrefix + "dirty";
    }

    public String retryKey() {
        return keyPrefix + "retry";
    }

    public String deadLetterKey() {
        return keyPrefix + "dlq";
    }

    public String recentKey() {
        return keyPrefix + "recent";
    }
}
