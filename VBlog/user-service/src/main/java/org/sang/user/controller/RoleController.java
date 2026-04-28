package org.sang.user.controller;

import org.sang.user.common.ApiResponse;
import org.sang.user.entity.Role;
import org.sang.user.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<Role>> getAllRoles() {
        return ApiResponse.success(roleService.getAllRole());
    }
}
