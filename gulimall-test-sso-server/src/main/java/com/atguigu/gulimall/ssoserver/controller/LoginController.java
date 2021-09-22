package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * @Classname LoginController
 * @Description TODO
 * @Date 2021/9/21 2:29 下午
 * @Created by tangyao
 */
@Controller
public class LoginController {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Model model) {
        model.addAttribute("url", url);
        return "login";
    }


    @PostMapping("/doLogin")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password, String url) {

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {

            // 把登陆的用户存进去
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid, username);
            // 登录成功，跳回到之前的页面
            return "redirect:" + url + "?token=" + uuid;
        }

        // 登录失败，跳回登录页
        return "login";
    }


}

