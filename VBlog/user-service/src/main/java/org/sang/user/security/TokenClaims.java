package org.sang.user.security;

import java.util.List;

public class TokenClaims {
    private Long userId;
    private String username;
    private List<String> roles;
    private long expiresAt;

    public TokenClaims(Long userId, String username, List<String> roles, long expiresAt) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.expiresAt = expiresAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
}
