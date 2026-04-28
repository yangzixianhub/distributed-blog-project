package org.sang.controller.admin;

import org.sang.bean.RespBean;
import org.sang.bean.Role;
import org.sang.bean.User;
import org.sang.client.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class UserManaController {
    @Autowired
    UserServiceClient userServiceClient;

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public List<User> getUserByNickname(String nickname, @RequestHeader("Authorization") String authorization) {
        return userServiceClient.listUsers(nickname, authorization);
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public User getUserById(@PathVariable Long id, @RequestHeader("Authorization") String authorization) {
        return userServiceClient.getUserById(id, authorization);
    }

    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    public List<Role> getAllRole(@RequestHeader("Authorization") String authorization) {
        return userServiceClient.listRoles(authorization);
    }

    @RequestMapping(value = "/user/enabled", method = RequestMethod.PUT)
    public RespBean updateUserEnabled(Boolean enabled, Long uid, @RequestHeader("Authorization") String authorization) {
        try {
            userServiceClient.updateUserEnabled(enabled, uid, authorization);
            return new RespBean("success", "修改成功!");
        } catch (Exception ex) {
            return new RespBean("error", ex.getMessage());
        }
    }

    @RequestMapping(value = "/user/{uid}", method = RequestMethod.DELETE)
    public RespBean deleteUserById(@PathVariable Long uid, @RequestHeader("Authorization") String authorization) {
        try {
            userServiceClient.deleteUser(uid, authorization);
            return new RespBean("success", "删除成功!");
        } catch (Exception ex) {
            return new RespBean("error", ex.getMessage());
        }
    }

    @RequestMapping(value = "/user/role", method = RequestMethod.PUT)
    public RespBean updateUserRoles(Long[] rids, Long id, @RequestHeader("Authorization") String authorization) {
        try {
            userServiceClient.updateUserRoles(rids, id, authorization);
            return new RespBean("success", "修改成功!");
        } catch (Exception ex) {
            return new RespBean("error", ex.getMessage());
        }
    }
}
