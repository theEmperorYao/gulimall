package com.atguigu.gulimall.auth.controller;

import com.atguigu.gulimall.auth.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname OAuth2Controller
 * @Description 社交登录请求
 * @Date 2021/9/15 12:50 上午
 * @Created by tangyao
 */
public class OAuth2Controller {

    @GetMapping("/oauth2.0/gittee/success")
    public String gittee(@RequestParam("code") String code) throws Exception {

        Map<String, String> map = new HashMap<>();
        //1、根据code换取accessToken;
        map.put("grant_type", "authorization_code");
        map.put("code", code);
        map.put("client_id", "5d452ed86ea65187523aa7b23f7b958257a66a350666a52b5c04566161f3c7a2");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gittee/success");
        map.put("client_secret", "5ca71b3a911d2a10fcf595952c5b24563b0acbce66e340c69ed08272a556ae25");

        HttpResponse post = HttpUtils.doPost("gitee.com", "oauth/token", "post", null, null, map);
        
        //2、登录成功就跳回首页
        return "redirect:http://gulimall.com";
    }
}
