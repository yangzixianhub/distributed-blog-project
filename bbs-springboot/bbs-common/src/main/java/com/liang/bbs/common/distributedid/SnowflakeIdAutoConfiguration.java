package com.liang.bbs.common.distributedid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(ObjectMapper.class)
@EnableConfigurationProperties(SnowflakeIdProperties.class)
public class SnowflakeIdAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeIdClient snowflakeIdClient(SnowflakeIdProperties properties, ObjectMapper objectMapper) {
        return new SnowflakeIdClient(properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeIdService snowflakeIdService(SnowflakeIdProperties properties, SnowflakeIdClient client) {
        return new SnowflakeIdService(properties, client);
    }
}
