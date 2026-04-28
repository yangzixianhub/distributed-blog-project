package org.sang.user.service;

import org.sang.user.entity.Role;
import org.sang.user.mapper.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleMapper roleMapper;

    public RoleService(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public List<Role> getRolesByUid(Long uid) {
        return roleMapper.getRolesByUid(uid);
    }

    public List<Role> getAllRole() {
        return roleMapper.getAllRole();
    }
}
