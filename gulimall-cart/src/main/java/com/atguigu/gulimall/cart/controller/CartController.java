package com.atguigu.gulimall.cart.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * @Classname CartController
 * @Description TODO
 * @Date 2021/9/26 5:41 下午
 * @Created by tangyao
 */
@Controller
public class CartController {

    /**
     * @return java.lang.String
     * @description 浏览器有一个cookie，user-key：标识用户身份，一个月之后过期
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份
     * 浏览器保存以后，每次访问都会带上这个cookie
     * <p>
     * 登录：session有
     * 没登录：按照cookie里面带来的user-key来做。
     * 第一次：如果没有临时用户，帮忙创建一个临时用户
     * @version V1.0.0
     * @date 6:57 下午 2021/9/26
     * @author tangyao
     */
    @GetMapping("cart.html")
    public String cartListPage() {

        //1、快速得到用户信息，id，user-key
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        System.out.println("userInfoTo = " + userInfoTo);

        return "cartList";
    }

    /**
     * @return java.lang.String
     * @description
     * @version V1.0.0
     * @date 10:40 下午 2021/9/26
     * @author tangyao
     */
    @GetMapping("/addToCart")
    public String addToCart() {
        return "success";
    }

}
