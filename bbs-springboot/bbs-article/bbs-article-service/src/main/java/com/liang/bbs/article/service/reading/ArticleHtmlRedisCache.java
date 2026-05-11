package com.liang.bbs.article.service.reading;

import com.liang.bbs.article.service.config.ArticleReadingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 已发布文章正文HTML的Redis缓存，减轻数据库读取。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleHtmlRedisCache {

    private static final String KEY_PREFIX = "bbs:article:html:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ArticleReadingProperties articleReadingProperties;

    public String get(Integer articleId) {
        if (!articleReadingProperties.getRedis().isHtmlCacheEnabled() || articleId == null) {
            return null;
        }
        String key = KEY_PREFIX + articleId;
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("读取文章HTML缓存失败 articleId={}", articleId, e);
            return null;
        }
    }

    public void put(Integer articleId, String html) {
        if (!articleReadingProperties.getRedis().isHtmlCacheEnabled() || articleId == null || StringUtils.isBlank(html)) {
            return;
        }
        Duration ttl = articleReadingProperties.getRedis().getTtl();
        String key = KEY_PREFIX + articleId;
        try {
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                stringRedisTemplate.opsForValue().set(key, html);
            } else {
                stringRedisTemplate.opsForValue().set(key, html, ttl.getSeconds(), TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("写入文章HTML缓存失败 articleId={}", articleId, e);
        }
    }

    public void evict(Integer articleId) {
        if (articleId == null) {
            return;
        }
        try {
            stringRedisTemplate.delete(KEY_PREFIX + articleId);
        } catch (Exception e) {
            log.warn("删除文章HTML缓存失败 articleId={}", articleId, e);
        }
    }
}
