package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @Classname CartInterceptor
 * @Description TODO
 * <p>
 * 在执行方法之前，判断用户的登录状态，并封装传递给controller目标
 * @Date 2021/9/26 7:01 下午
 * @Created by tangyao
 */

public class CartInterceptor implements HandlerInterceptor {


    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * @return boolean
     * @description 目标方法执行之前
     * @version V1.0.0
     * @date 7:03 下午 2021/9/26
     * @author tangyao
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        MemberRespVo memberRespVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberRespVo != null) {
            // 用户登录
            userInfoTo.setUserId(memberRespVo.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
        for (Cookie cookie : cookies) {
                //user-key
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }

            }
        }

        // 如果没有临时用户一定分配一个临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String s = UUID.randomUUID().toString();
            userInfoTo.setUserKey(s);
        }

        // 目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (userInfoTo.getTempUser()) {
            return;
        }
        // 如果没有临时用户，一定保存一个临时用户
        // 持续的延长临时用户的过期时间
        Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
        cookie.setDomain("gulimall.com");
        cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
        response.addCookie(cookie);
    }
}
