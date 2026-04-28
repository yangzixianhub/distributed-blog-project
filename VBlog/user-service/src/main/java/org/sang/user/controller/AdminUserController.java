package org.sang.user.controller;

import org.sang.user.common.ApiResponse;
import org.sang.user.dto.UserDTO;
import org.sang.user.entity.User;
import org.sang.user.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserDTO>> list(@RequestParam(required = false) String nickname) {
        List<UserDTO> users = new ArrayList<>();
        for (User user : userService.listByNickname(nickname)) {
            users.add(UserDTO.from(user));
        }
        return ApiResponse.success(users);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return ApiResponse.success(UserDTO.from(user));
    }

    @PutMapping("/{id}/enabled")
    public ApiResponse<Object> updateEnabled(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        userService.updateUserEnabled(body.get("enabled"), id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<Object> updateRoles(@PathVariable Long id, @RequestBody Map<String, Long[]> body) {
        userService.updateUserRoles(body.get("roleIds"), id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ApiResponse.success(null);
    }
}
