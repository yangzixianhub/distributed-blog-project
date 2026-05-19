package com.liang.bbs.user.service.like;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class LikeCacheCoordinator {
    private static final int MAX_RETRY = 3;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    public long getCount(LikeTargetType type, Integer targetId, LikeStateRepository repository) {
        try {
            Object cached = redisTemplate.opsForValue().get(type.countKey(targetId));
            if (cached instanceof Number) {
                return ((Number) cached).longValue();
            }
            long count = repository.loadCount(targetId);
            redisTemplate.opsForValue().set(type.countKey(targetId), count);
            return count;
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable when reading {} like count, fallback to DB", type, ex);
            return repository.loadCount(targetId);
        }
    }

    public boolean isLiked(LikeTargetType type, Integer targetId, Long userId, LikeStateRepository repository) {
        try {
            Object cached = redisTemplate.opsForHash().get(type.stateKey(targetId), userId.toString());
            if (cached != null) {
                return Boolean.parseBoolean(String.valueOf(cached));
            }
            boolean state = repository.loadState(targetId, userId);
            redisTemplate.opsForHash().put(type.stateKey(targetId), userId.toString(), state);
            return state;
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable when reading {} like state, fallback to DB", type, ex);
            return repository.loadState(targetId, userId);
        }
    }

    public boolean toggle(LikeTargetType type, Integer targetId, Long userId, LikeStateRepository repository) {
        RLock lock = redissonClient.getLock("like_toggle:" + type.name() + ":" + targetId + ":" + userId);
        try {
            lock.lock();
            boolean currentState = isLiked(type, targetId, userId, repository);
            long currentCount = getCount(type, targetId, repository);
            boolean nextState = !currentState;
            redisTemplate.opsForHash().put(type.stateKey(targetId), userId.toString(), nextState);
            redisTemplate.opsForValue().set(type.countKey(targetId), Math.max(0, currentCount + (nextState ? 1 : -1)));
            redisTemplate.opsForHash().put(type.dirtyKey(), dirtyField(targetId, userId), nextState);
            redisTemplate.opsForSet().add(type.recentKey(), targetId.toString());
            return true;
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable when toggling {} like, fallback to DB sync path", type, ex);
            boolean nextState = !repository.loadState(targetId, userId);
            repository.upsertState(targetId, userId, nextState);
            return true;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void flush(LikeTargetType type, LikeStateRepository repository) {
        Map<Object, Object> dirtyEntries = redisTemplate.opsForHash().entries(type.dirtyKey());
        for (Map.Entry<Object, Object> entry : dirtyEntries.entrySet()) {
            String field = String.valueOf(entry.getKey());
            String[] parts = field.split(":", 2);
            Integer targetId = Integer.valueOf(parts[0]);
            Long userId = Long.valueOf(parts[1]);
            boolean state = Boolean.parseBoolean(String.valueOf(entry.getValue()));
            try {
                repository.upsertState(targetId, userId, state);
                redisTemplate.opsForHash().delete(type.dirtyKey(), field);
                redisTemplate.opsForHash().delete(type.retryKey(), field);
            } catch (Exception ex) {
                int retry = nextRetry(type, field);
                log.error("Flush {} like failed, targetId={}, userId={}, retry={}", type, targetId, userId, retry, ex);
                if (retry >= MAX_RETRY) {
                    redisTemplate.opsForList().rightPush(type.deadLetterKey(), field + ":" + state);
                    redisTemplate.opsForHash().delete(type.dirtyKey(), field);
                }
            }
        }
    }

    public void reconcileRecent(LikeTargetType type, LikeStateRepository repository) {
        Set<Object> recentTargetIds = redisTemplate.opsForSet().members(type.recentKey());
        if (recentTargetIds == null) {
            return;
        }
        for (Object targetIdValue : recentTargetIds) {
            Integer targetId = Integer.valueOf(String.valueOf(targetIdValue));
            if (hasPendingDirty(type, targetId)) {
                continue;
            }
            long dbCount = repository.loadCount(targetId);
            Object cached = redisTemplate.opsForValue().get(type.countKey(targetId));
            long cachedCount = cached instanceof Number ? ((Number) cached).longValue() : -1L;
            if (cachedCount != dbCount) {
                log.warn("Reconcile {} like count, targetId={}, redis={}, db={}", type, targetId, cachedCount, dbCount);
                redisTemplate.opsForValue().set(type.countKey(targetId), dbCount);
            }
        }
    }

    private int nextRetry(LikeTargetType type, String field) {
        Long retry = redisTemplate.opsForHash().increment(type.retryKey(), field, 1L);
        return retry == null ? 1 : retry.intValue();
    }

    private boolean hasPendingDirty(LikeTargetType type, Integer targetId) {
        Set<Object> dirtyFields = redisTemplate.opsForHash().keys(type.dirtyKey());
        if (dirtyFields == null) {
            return false;
        }
        String prefix = targetId + ":";
        for (Object field : dirtyFields) {
            if (String.valueOf(field).startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String dirtyField(Integer targetId, Long userId) {
        return targetId + ":" + userId;
    }
}
