package org.sang.user.dto;

import java.util.List;

public class TokenVerifyResponse {
    private Long userId;
    private String username;
    private List<String> roles;
    private long expiresAt;

    public TokenVerifyResponse() {
    }

    public TokenVerifyResponse(Long userId, String username, List<String> roles, long expiresAt) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.expiresAt = expiresAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
