package com.liang.bbs.article.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 文章阅读增强：静态HTML与Redis正文缓存。
 */
@Data
@ConfigurationProperties(prefix = "article.reading")
public class ArticleReadingProperties {

    /**
     * 静态页生成：审核通过后写入本地目录，由Nginx映射为对外URL。
     */
    private StaticHtml staticHtml = new StaticHtml();

    /**
     * Redis：缓存已发布文章正文HTML，减轻Mongo读压力。
     */
    private Redis redis = new Redis();

    @Data
    public static class StaticHtml {
        /**
         * 是否生成静态文件（默认关闭，部署时再打开）。
         */
        private boolean enabled = false;
        /**
         * 静态HTML输出目录（可与Nginx root指向同一目录）。
         */
        private String directory = "./article-static-html";
        /**
         * 浏览器访问路径前缀，例如 /static/articles ，需与Nginx location一致。
         */
        private String publicUrlPrefix = "/static/articles";
    }

    @Data
    public static class Redis {
        /**
         * 是否缓存正文HTML（非文章作者读详情时生效）。
         */
        private boolean htmlCacheEnabled = true;
        /**
         * 正文缓存TTL。
         */
        private Duration ttl = Duration.ofMinutes(30);
    }
}
