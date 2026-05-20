package com.liang.bbs.common.distributedid;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "distributed-id")
public class SnowflakeIdProperties {
    private boolean enabled = false;

    private String baseUrl = "http://127.0.0.1:5000";

    private int connectTimeoutMs = 5000;

    private int readTimeoutMs = 5000;

    private ApiKeys apiKeys = new ApiKeys();

    @Data
    public static class ApiKeys {
        private String article = "blog-article-service-key";
        private String comment = "blog-comment-service-key";
        private String like = "blog-like-service-key";
    }

    public String resolveApiKey(SnowflakeIdScope scope) {
        if (scope == null) {
            return apiKeys.getLike();
        }
        switch (scope) {
            case ARTICLE:
                return apiKeys.getArticle();
            case COMMENT:
                return apiKeys.getComment();
            case LIKE:
            default:
                return apiKeys.getLike();
        }
    }
}
