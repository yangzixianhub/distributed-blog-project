package com.liang.bbs.common.distributedid;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.LongConsumer;

@Slf4j
public class SnowflakeIdService {
    private final SnowflakeIdProperties properties;
    private final SnowflakeIdClient client;

    public SnowflakeIdService(SnowflakeIdProperties properties, SnowflakeIdClient client) {
        this.properties = properties;
        this.client = client;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public Optional<Long> tryNextId(SnowflakeIdScope scope) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }
        try {
            return Optional.of(client.nextId(scope));
        } catch (Exception ex) {
            log.error("Snowflake id service unavailable for scope {}", scope, ex);
            return Optional.empty();
        }
    }

    public long nextId(SnowflakeIdScope scope) {
        return tryNextId(scope).orElseThrow(() ->
                new IllegalStateException("Snowflake id is disabled or unavailable for scope " + scope));
    }

    public void assignPrimaryKey(LongConsumer idSetter, SnowflakeIdScope scope) {
        tryNextId(scope).ifPresent(id -> idSetter.accept(id));
    }
}
