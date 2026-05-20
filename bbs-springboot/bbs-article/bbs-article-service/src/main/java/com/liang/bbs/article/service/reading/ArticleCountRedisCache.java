package com.liang.bbs.article.service.reading;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liang.bbs.article.service.config.ArticleReadingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

//缓存文章公共统计（点赞数、评论数、作者等级）
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCountRedisCache {

    private static final String KEY_PREFIX = "bbs:article:count:pub:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ArticleReadingProperties articleReadingProperties;

    public ArticleCountPublicSnapshot get(Integer articleId) {
        if (!isEnabled() || articleId == null) {
            return null;
        }
        String key = KEY_PREFIX + articleId;
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null || json.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(json, ArticleCountPublicSnapshot.class);
        } catch (JsonProcessingException e) {
            log.warn("反序列化文章统计缓存失败 articleId={}", articleId, e);
            return null;
        } catch (Exception e) {
            log.warn("读取文章统计缓存失败 articleId={}", articleId, e);
            return null;
        }
    }

    public void put(Integer articleId, ArticleCountPublicSnapshot snapshot) {
        if (!isEnabled() || articleId == null || snapshot == null) {
            return;
        }
        Duration ttl = articleReadingProperties.getRedis().getCountCacheTtl();
        String key = KEY_PREFIX + articleId;
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                stringRedisTemplate.opsForValue().set(key, json);
            } else {
                stringRedisTemplate.opsForValue().set(key, json, ttl.getSeconds(), TimeUnit.SECONDS);
            }
        } catch (JsonProcessingException e) {
            log.warn("序列化文章统计缓存失败 articleId={}", articleId, e);
        } catch (Exception e) {
            log.warn("写入文章统计缓存失败 articleId={}", articleId, e);
        }
    }

    public void evict(Integer articleId) {
        if (articleId == null) {
            return;
        }
        try {
            stringRedisTemplate.delete(KEY_PREFIX + articleId);
        } catch (Exception e) {
            log.warn("删除文章统计缓存失败 articleId={}", articleId, e);
        }
    }

    private boolean isEnabled() {
        return articleReadingProperties.getRedis().isCountCacheEnabled();
    }
}
