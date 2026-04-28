package org.sang.user.controller;

import org.sang.user.common.ApiResponse;
import org.sang.user.dto.LoginRequest;
import org.sang.user.dto.LoginResponse;
import org.sang.user.dto.RegisterRequest;
import org.sang.user.dto.TokenVerifyResponse;
import org.sang.user.dto.UserDTO;
import org.sang.user.service.AuthService;
import org.sang.user.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/verify")
    public ApiResponse<TokenVerifyResponse> verify(@RequestHeader("Authorization") String authorization) {
        return ApiResponse.success(authService.verify(resolveToken(authorization)));
    }

    @PostMapping("/logout")
    public ApiResponse<Object> logout() {
        return ApiResponse.success(null);
    }

    private String resolveToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalStateException("未登录或 token 无效");
        }
        return authorization.substring(7);
    }
}
