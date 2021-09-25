package com.atguigu.gulimall.ssoserver.controller;

import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
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


    @ResponseBody
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("token") String token) {
        return stringRedisTemplate.opsForValue().get(token);
    }


    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url,
                            Model model,
                            @CookieValue(value = "sso_token", required = false) String sso_token) {

        if (!StringUtils.isEmpty(sso_token)) {
            //说明之前有人登录过，浏览器留下了痕迹
            return "redirect:" + url + "?token=" + sso_token;
        }
        model.addAttribute("url", url);
        return "login";
    }


    @PostMapping("/doLogin")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam("url") String url,
                        HttpServletResponse response) {

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {

            // 把登陆的用户存进去
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid, username);

            Cookie cookie = new Cookie("sso_token", uuid);
            response.addCookie(cookie);
            // 登录成功，跳回到之前的页面
            return "redirect:" + url + "?token=" + uuid;
        }

        // 登录失败，跳回登录页
        return "login";
    }


}

