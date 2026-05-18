package com.liang.bbs.article.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Elasticsearch basic connection properties.
 */
@Data
@ConfigurationProperties(prefix = "search.elasticsearch")
public class ElasticsearchProperties {
    private boolean enabled = false;
    private String uris = "http://127.0.0.1:9200";
    private String username;
    private String password;
    private Integer connectTimeout = 1000;
    private Integer socketTimeout = 30000;
    private String articleIndex = "bbs_article";
    private Integer syncRetryCount = 3;
    private Long syncRetryBackoffMillis = 1000L;
}
