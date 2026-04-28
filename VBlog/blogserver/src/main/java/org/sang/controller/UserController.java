package org.sang.controller;

import org.sang.bean.RespBean;
import org.sang.bean.Role;
import org.sang.bean.User;
import org.sang.client.UserServiceClient;
import org.sang.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserServiceClient userServiceClient;

    @RequestMapping("/currentUserName")
    public String currentUserName(@RequestHeader("Authorization") String authorization) {
        return userServiceClient.currentUser(authorization).getNickname();
    }

    @RequestMapping("/currentUserId")
    public Long currentUserId() {
        return Util.getCurrentUser().getId();
    }

    @RequestMapping("/currentUserEmail")
    public String currentUserEmail(@RequestHeader("Authorization") String authorization) {
        return userServiceClient.currentUser(authorization).getEmail();
    }

    @RequestMapping("/isAdmin")
    public Boolean isAdmin() {
        User currentUser = Util.getCurrentUser();
        if (currentUser.getRoles() == null) {
            return false;
        }
        for (Role role : currentUser.getRoles()) {
            if ("ADMIN".equals(role.getName())) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(value = "/updateUserEmail", method = RequestMethod.PUT)
    public RespBean updateUserEmail(String email, @RequestHeader("Authorization") String authorization) {
        try {
            userServiceClient.updateCurrentUserEmail(email, authorization);
            return new RespBean("success", "修改成功!");
        } catch (Exception ex) {
            return new RespBean("error", ex.getMessage());
        }
    }
}
