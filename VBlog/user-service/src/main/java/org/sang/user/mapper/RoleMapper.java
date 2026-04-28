package org.sang.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sang.user.entity.Role;

import java.util.List;

@Mapper
public interface RoleMapper {
    int addRoles(@Param("roles") Long[] roles, @Param("uid") Long uid);

    List<Role> getRolesByUid(@Param("uid") Long uid);

    List<Role> getAllRole();
}
