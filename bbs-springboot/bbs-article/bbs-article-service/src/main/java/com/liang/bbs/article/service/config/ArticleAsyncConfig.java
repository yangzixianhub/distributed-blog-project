package com.liang.bbs.article.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async executor for non-blocking article side effects such as ES sync.
 */
@Configuration
@EnableAsync
public class ArticleAsyncConfig {

    @Bean("articleSearchSyncExecutor")
    public Executor articleSearchSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("article-search-sync-");
        executor.initialize();
        return executor;
    }
}
