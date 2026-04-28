package org.sang.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sang.user.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
    User loadUserByUsername(@Param("username") String username);

    long insertUser(User user);

    int updateUserEmail(@Param("email") String email, @Param("id") Long id);

    List<User> getUserByNickname(@Param("nickname") String nickname);

    int updateUserEnabled(@Param("enabled") Boolean enabled, @Param("uid") Long uid);

    int deleteUserById(@Param("id") Long uid);

    int deleteUserRolesByUid(@Param("id") Long id);

    int setUserRoles(@Param("rids") Long[] rids, @Param("id") Long id);

    User getUserById(@Param("id") Long id);
}
