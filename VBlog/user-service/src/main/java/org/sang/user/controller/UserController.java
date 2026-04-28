package org.sang.user.controller;

import org.sang.user.common.ApiResponse;
import org.sang.user.dto.UserDTO;
import org.sang.user.entity.Role;
import org.sang.user.entity.User;
import org.sang.user.security.CurrentUser;
import org.sang.user.security.UserPrincipal;
import org.sang.user.service.RoleService;
import org.sang.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/me")
    public ApiResponse<UserDTO> me() {
        UserPrincipal principal = CurrentUser.get();
        return ApiResponse.success(UserDTO.from(userService.getById(principal.getUserId())));
    }

    @GetMapping("/me/roles")
    public ApiResponse<List<Role>> myRoles() {
        UserPrincipal principal = CurrentUser.get();
        return ApiResponse.success(roleService.getRolesByUid(principal.getUserId()));
    }

    @PutMapping("/me/email")
    public ApiResponse<UserDTO> updateEmail(@RequestBody Map<String, String> body) {
        UserPrincipal principal = CurrentUser.get();
        return ApiResponse.success(userService.updateEmail(principal.getUserId(), body.get("email")));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return ApiResponse.success(UserDTO.from(user));
    }

    @GetMapping("/by-username/{username}")
    public ApiResponse<UserDTO> getByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return ApiResponse.success(UserDTO.from(user));
    }
}
