package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.util.HttpUtils;
import com.atguigu.gulimall.auth.vo.MemberRespVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.SocialUserDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname OAuth2Controller
 * @Description 社交登录请求
 * @Date 2021/9/15 12:50 上午
 * @Created by tangyao
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * @description 社交登录成功回调
     * @param code
     * @return java.lang.String
     * @version V1.0.0
     * @date 10:38 下午 2021/9/18
     * @author tangyao
     */
    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String, String> map = new HashMap<>();
        Map<String, String> header = new HashMap<>();

        //https://gitee.com/oauth/token
        // ?grant_type=authorization_code
        // &code=9e3923a12e3b0658ff7b666991963d8e
        // &client_id=5d452ed86ea65187523aa7b23f7b958257a66a350666a52b5c04566161f3c7a2
        // &redirect_uri=http://auth.gulimall.com/oauth2.0/gitee/success
        // &client_secret=5ca71b3a911d2a10fcf595952c5b24563b0acbce66e340c69ed08272a556ae25

        //1、根据code换取accessToken;
        map.put("grant_type", "authorization_code");
        map.put("code", code);
        map.put("client_id", "5d452ed86ea65187523aa7b23f7b958257a66a350666a52b5c04566161f3c7a2");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/success");
        map.put("client_secret", "5ca71b3a911d2a10fcf595952c5b24563b0acbce66e340c69ed08272a556ae25");

        HttpResponse response = HttpUtils.doPost("https://gitee.com/", "oauth/token", "post", header, null, map);

        if (response.getStatusLine().getStatusCode() == 200) {
            //获取到accessCodeToken
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            String token = socialUser.getAccessToken();

            if (Strings.isNotEmpty(token)) {
                // https://gitee.com/api/v5/user
                Map<String, String> queryMap = new HashMap<>();
                header = new HashMap<>();
                queryMap.put("access_token", token);
                response = HttpUtils.doGet("https://gitee.com/", "api/v5/user", "get", header, queryMap);
                json = EntityUtils.toString(response.getEntity());
                SocialUserDetail socialUserDetail = JSON.parseObject(json, SocialUserDetail.class);

                socialUser.setId(socialUserDetail.getId().toString());

                // 知道当前是哪个社交用户
                //1)、当前用户如果第一次进网站，自动注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定会员）
                R r = memberFeignService.oauthLogin(socialUser);
                if (r.getCode() == 0) {
                    MemberRespVo data = r.getData(new TypeReference<MemberRespVo>() {
                    });
                    System.out.println("登录成功:用户信息：" + data);
                    log.info("登录成功:用户信息：{}", data.toString());

                    session.setAttribute("loginUser",data);
                    //2、登录成功就跳回首页
                    return "redirect:http://gulimall.com";
                }
            }
        }
        // 失败处理
        return "redirect:http://auth.gulimall.com/login.html";


    }

}



















