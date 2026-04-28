package org.sang.user.dto;

import org.sang.user.entity.Role;
import org.sang.user.entity.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private boolean enabled;
    private String email;
    private String userface;
    private Timestamp regTime;
    private List<String> roles;

    public static UserDTO from(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEnabled(user.isEnabled());
        dto.setEmail(user.getEmail());
        dto.setUserface(user.getUserface());
        dto.setRegTime(user.getRegTime());
        List<String> roleNames = new ArrayList<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                roleNames.add(toAuthority(role.getName()));
            }
        }
        dto.setRoles(roleNames);
        return dto;
    }

    public static String toAuthority(String roleName) {
        if (roleName == null || roleName.startsWith("ROLE_")) {
            return roleName;
        }
        return "ROLE_" + roleName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserface() {
        return userface;
    }

    public void setUserface(String userface) {
        this.userface = userface;
    }

    public Timestamp getRegTime() {
        return regTime;
    }

    public void setRegTime(Timestamp regTime) {
        this.regTime = regTime;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
