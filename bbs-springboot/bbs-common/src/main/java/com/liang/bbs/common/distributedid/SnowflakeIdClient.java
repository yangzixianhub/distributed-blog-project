package com.liang.bbs.common.distributedid;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class SnowflakeIdClient {
    private final SnowflakeIdProperties properties;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    public SnowflakeIdClient(SnowflakeIdProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(properties.getConnectTimeoutMs()))
                .setResponseTimeout(Timeout.ofMilliseconds(properties.getReadTimeoutMs()))
                .build();
        this.httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
    }

    public long nextId(SnowflakeIdScope scope) {
        String url = trimSlash(properties.getBaseUrl()) + "/api/id";
        HttpGet request = new HttpGet(url);
        request.addHeader("X-API-Key", properties.resolveApiKey(scope));
        return executeIdRequest(request);
    }

    public List<Long> nextIds(SnowflakeIdScope scope, int count) {
        if (count <= 0 || count > 1000) {
            throw new IllegalArgumentException("count must be between 1 and 1000");
        }
        String url = trimSlash(properties.getBaseUrl()) + "/api/id/batch";
        HttpPost request = new HttpPost(url);
        request.addHeader("X-API-Key", properties.resolveApiKey(scope));
        request.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        try {
            request.setEntity(new StringEntity("{\"count\":" + count + "}", ContentType.APPLICATION_JSON));
            return httpClient.execute(request, response -> parseResponse(response,
                    new TypeReference<SnowflakeIdResponse<List<Long>>>() {
                    }));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to batch generate snowflake ids", ex);
        }
    }

    private long executeIdRequest(HttpGet request) {
        try {
            return httpClient.execute(request, response -> parseResponse(response,
                    new TypeReference<SnowflakeIdResponse<Long>>() {
                    }));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate snowflake id", ex);
        }
    }

    private <T> T parseResponse(ClassicHttpResponse response, TypeReference<SnowflakeIdResponse<T>> type) {
        try {
            return execute(response, type);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse snowflake id response", ex);
        }
    }

    private <T> T execute(ClassicHttpResponse response, TypeReference<SnowflakeIdResponse<T>> type)
            throws Exception {
        int status = response.getCode();
        String body = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("Snowflake id service HTTP " + status + ": " + body);
        }
        SnowflakeIdResponse<T> parsed = objectMapper.readValue(body, type);
        if (parsed == null || !parsed.isSuccess() || parsed.getData() == null) {
            String message = parsed == null ? "empty response" : parsed.getMessage();
            throw new IllegalStateException("Snowflake id service rejected request: " + message);
        }
        return parsed.getData();
    }

    private static String trimSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return "http://127.0.0.1:5000";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
