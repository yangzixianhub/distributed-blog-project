package org.sang.controller;

import org.sang.bean.RespBean;
import org.sang.bean.User;
import org.sang.client.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginRegController {

    @Autowired
    UserServiceClient userServiceClient;

    @PostMapping("/login")
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = userServiceClient.login(username, password);
            result.put("status", "success");
            result.put("msg", "登录成功");
            result.put("data", data);
        } catch (Exception ex) {
            result.put("status", "error");
            result.put("msg", ex.getMessage());
        }
        return result;
    }

    @RequestMapping("/login_error")
    public RespBean loginError() {
        return new RespBean("error", "登录失败!");
    }

    @RequestMapping("/login_success")
    public RespBean loginSuccess() {
        return new RespBean("success", "登录成功!");
    }

    @RequestMapping("/login_page")
    public RespBean loginPage() {
        return new RespBean("error", "尚未登录，请先登录!");
    }

    @RequestMapping("/logout")
    public RespBean logout() {
        return new RespBean("success", "退出成功!");
    }

    @PostMapping("/reg")
    public RespBean reg(User user) {
        try {
            userServiceClient.register(user);
            return new RespBean("success", "注册成功!");
        } catch (Exception ex) {
            return new RespBean("error", ex.getMessage());
        }
    }
}
