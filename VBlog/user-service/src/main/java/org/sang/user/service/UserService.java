package org.sang.user.service;

import org.sang.user.dto.RegisterRequest;
import org.sang.user.dto.UserDTO;
import org.sang.user.entity.User;
import org.sang.user.mapper.RoleMapper;
import org.sang.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {
    private static final Long DEFAULT_USER_ROLE_ID = 2L;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, RoleMapper roleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User getByUsername(String username) {
        return userMapper.loadUserByUsername(username);
    }

    public User getById(Long id) {
        return userMapper.getUserById(id);
    }

    public UserDTO register(RegisterRequest request) {
        validateRegister(request);
        User exists = userMapper.loadUserByUsername(request.getUsername());
        if (exists != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setEnabled(true);

        userMapper.insertUser(user);
        roleMapper.addRoles(new Long[]{DEFAULT_USER_ROLE_ID}, user.getId());
        return UserDTO.from(userMapper.getUserById(user.getId()));
    }

    public UserDTO updateEmail(Long userId, String email) {
        userMapper.updateUserEmail(email, userId);
        return UserDTO.from(userMapper.getUserById(userId));
    }

    public List<User> listByNickname(String nickname) {
        return userMapper.getUserByNickname(nickname);
    }

    public int updateUserEnabled(Boolean enabled, Long uid) {
        return userMapper.updateUserEnabled(enabled, uid);
    }

    public int deleteUserById(Long uid) {
        return userMapper.deleteUserById(uid);
    }

    public int updateUserRoles(Long[] rids, Long id) {
        userMapper.deleteUserRolesByUid(id);
        if (rids == null || rids.length == 0) {
            return 0;
        }
        return userMapper.setUserRoles(rids, id);
    }

    private void validateRegister(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
    }
}
