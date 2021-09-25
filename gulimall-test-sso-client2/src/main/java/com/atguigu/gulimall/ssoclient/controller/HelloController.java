package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @Classname HelloController
 * @Description TODO
 * @Date 2021/9/21 2:06 下午
 * @Created by tangyao
 */
@Controller
public class HelloController {


    @Value("${sso.server.url}")
    String ssoServerUrl;


    /**
     * @return java.lang.String
     * @description 无需登录就可访问
     * @version V1.0.0
     * @date 2:07 下午 2021/9/21
     * @author tangyao
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }


    /**
     * @return java.lang.String
     * @description 感知这次在ssoserver登录成功跳回来的
     * @version V1.0.0
     * @date 6:13 下午 2021/9/22
     * @author tangyao
     */
    @GetMapping("/boss")
    public String employees(Model model,
                            @RequestParam(value = "token", required = false) String token,
                            HttpSession session) {

        if (!StringUtils.isEmpty(token)) {
            //去ssoserver登录成功跳回来就会带上
            //1、todo 去ssoserver获取当前token真正对应的用户信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity =
                    restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();

            session.setAttribute("loginUser", body);
        }

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            //没登录,跳转到登录服务器进行登录

            //跳转过去，使用url上的查询参数标识我们自己是哪个页面redirect_url
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client2.com:8082/boss";
        } else {
            List<String> emps = new ArrayList<>();
            emps.add("张三");
            emps.add("李四");
            model.addAttribute("emps", emps);
            return "list";
        }


    }


}
