package org.sang.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sang.bean.Role;
import org.sang.bean.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserServiceClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${user.service.base-url:http://localhost:8082}")
    private String userServiceBaseUrl;

    public UserServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        return postForData("/auth/login", body, null);
    }

    public User register(User user) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", user.getUsername());
        body.put("password", user.getPassword());
        body.put("nickname", user.getNickname());
        body.put("email", user.getEmail());
        return convertUser(postForData("/auth/register", body, null));
    }

    public Map<String, Object> verify(String authorization) {
        return postForData("/auth/verify", new HashMap<>(), authorization);
    }

    public User currentUser(String authorization) {
        return convertUser(exchangeForData("/users/me", HttpMethod.GET, null, authorization));
    }

    public User getUserById(Long id, String authorization) {
        return convertUser(exchangeForData("/admin/users/" + id, HttpMethod.GET, null, authorization));
    }

    public List<User> listUsers(String nickname, String authorization) {
        String keyword = nickname == null ? "" : nickname;
        Object data = exchangeForData("/admin/users?nickname=" + keyword, HttpMethod.GET, null, authorization);
        List<User> users = new ArrayList<>();
        if (data instanceof List) {
            for (Object item : (List<?>) data) {
                users.add(convertUser(item));
            }
        }
        return users;
    }

    public List<Role> listRoles(String authorization) {
        Object data = exchangeForData("/admin/roles", HttpMethod.GET, null, authorization);
        List<Role> roles = new ArrayList<>();
        if (data instanceof List) {
            for (Object item : (List<?>) data) {
                roles.add(convert(item, Role.class));
            }
        }
        return roles;
    }

    public void updateUserEnabled(Boolean enabled, Long uid, String authorization) {
        Map<String, Object> body = new HashMap<>();
        body.put("enabled", enabled);
        exchangeForData("/admin/users/" + uid + "/enabled", HttpMethod.PUT, body, authorization);
    }

    public void updateUserRoles(Long[] roleIds, Long uid, String authorization) {
        Map<String, Object> body = new HashMap<>();
        body.put("roleIds", roleIds);
        exchangeForData("/admin/users/" + uid + "/roles", HttpMethod.PUT, body, authorization);
    }

    public void deleteUser(Long uid, String authorization) {
        exchangeForData("/admin/users/" + uid, HttpMethod.DELETE, null, authorization);
    }

    public void updateCurrentUserEmail(String email, String authorization) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        exchangeForData("/users/me/email", HttpMethod.PUT, body, authorization);
    }

    private Map<String, Object> postForData(String path, Object body, String authorization) {
        Object data = exchangeForData(path, HttpMethod.POST, body, authorization);
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return new HashMap<>();
    }

    private Object exchangeForData(String path, HttpMethod method, Object body, String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authorization != null && !authorization.isEmpty()) {
            headers.set("Authorization", authorization);
        }
        ResponseEntity<Map> response = restTemplate.exchange(
                userServiceBaseUrl + path,
                method,
                new HttpEntity<>(body, headers),
                Map.class
        );
        Map responseBody = response.getBody();
        if (responseBody == null || !Integer.valueOf(200).equals(responseBody.get("code"))) {
            String message = responseBody == null ? "user-service 调用失败" : String.valueOf(responseBody.get("message"));
            throw new IllegalStateException(message);
        }
        return responseBody.get("data");
    }

    private <T> T convert(Object value, Class<T> clazz) {
        return objectMapper.convertValue(value, clazz);
    }

    private User convertUser(Object value) {
        Map userMap = objectMapper.convertValue(value, Map.class);
        User user = new User();
        Object id = userMap.get("id");
        if (id instanceof Number) {
            user.setId(((Number) id).longValue());
        }
        user.setUsername(stringValue(userMap.get("username")));
        user.setNickname(stringValue(userMap.get("nickname")));
        user.setEmail(stringValue(userMap.get("email")));
        user.setUserface(stringValue(userMap.get("userface")));
        Object enabled = userMap.get("enabled");
        user.setEnabled(enabled instanceof Boolean ? (Boolean) enabled : Boolean.parseBoolean(String.valueOf(enabled)));

        List<Role> roles = new ArrayList<>();
        Object roleValues = userMap.get("roles");
        if (roleValues instanceof List) {
            for (Object roleValue : (List<?>) roleValues) {
                if (roleValue instanceof Map) {
                    roles.add(convert(roleValue, Role.class));
                } else {
                    roles.add(roleFromAuthority(String.valueOf(roleValue)));
                }
            }
        }
        user.setRoles(roles);
        return user;
    }

    private Role roleFromAuthority(String authority) {
        String roleName = authority;
        if (roleName.startsWith("ROLE_")) {
            roleName = roleName.substring(5);
        }
        Long roleId = null;
        if ("ADMIN".equals(roleName)) {
            roleId = 1L;
        } else if ("USER".equals(roleName)) {
            roleId = 2L;
        }
        return new Role(roleId, roleName);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
