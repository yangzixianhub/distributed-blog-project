package org.sang.user.service;

import org.sang.user.dto.LoginRequest;
import org.sang.user.dto.LoginResponse;
import org.sang.user.dto.TokenVerifyResponse;
import org.sang.user.dto.UserDTO;
import org.sang.user.entity.User;
import org.sang.user.security.JwtUtils;
import org.sang.user.security.TokenClaims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalStateException("用户名或密码错误");
        }

        User user = userService.getByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalStateException("用户名或密码错误");
        }
        if (!user.isEnabled()) {
            throw new IllegalStateException("用户已被禁用");
        }

        UserDTO userDTO = UserDTO.from(user);
        String token = jwtUtils.generateToken(userDTO);
        return new LoginResponse(token, jwtUtils.getExpirationSeconds(), userDTO);
    }

    public TokenVerifyResponse verify(String token) {
        TokenClaims claims = jwtUtils.parseToken(token);
        return new TokenVerifyResponse(claims.getUserId(), claims.getUsername(), claims.getRoles(), claims.getExpiresAt());
    }
}
