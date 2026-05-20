package com.liang.bbs.user.service.scheduler;

import com.liang.bbs.user.service.like.ArticleLikeStateRepository;
import com.liang.bbs.user.service.like.CommentLikeStateRepository;
import com.liang.bbs.user.service.like.LikeCacheCoordinator;
import com.liang.bbs.user.service.like.LikeTargetType;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LikeFlushWorker {
    @Autowired
    private LikeCacheCoordinator likeCacheCoordinator;

    @Autowired
    private ArticleLikeStateRepository articleLikeStateRepository;

    @Autowired
    private CommentLikeStateRepository commentLikeStateRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Async("asyncTaskExecutor")
    @Scheduled(cron = "*/10 * * * * ?")
    public void flush() {
        RLock lock = redissonClient.getLock("like_flush_worker");
        try {
            if (lock.tryLock()) {
                likeCacheCoordinator.flush(LikeTargetType.ARTICLE, articleLikeStateRepository);
                likeCacheCoordinator.flush(LikeTargetType.COMMENT, commentLikeStateRepository);
            }
        } catch (Exception ex) {
            log.error("Like flush worker failed", ex);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Async("asyncTaskExecutor")
    @Scheduled(cron = "0 */5 * * * ?")
    public void reconcile() {
        RLock lock = redissonClient.getLock("like_reconcile_worker");
        try {
            if (lock.tryLock()) {
                likeCacheCoordinator.reconcileRecent(LikeTargetType.ARTICLE, articleLikeStateRepository);
                likeCacheCoordinator.reconcileRecent(LikeTargetType.COMMENT, commentLikeStateRepository);
            }
        } catch (Exception ex) {
            log.error("Like reconcile worker failed", ex);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
