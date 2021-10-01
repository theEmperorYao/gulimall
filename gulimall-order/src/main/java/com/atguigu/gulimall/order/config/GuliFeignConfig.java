package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Classname GuliFeignConfig
 * @Description TODO
 * @Date 2021/10/1 10:40 下午
 * @Created by tangyao
 */
@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptors")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //1、RequestContextHolder拿来刚进来的这个请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                // 老请求
                HttpServletRequest request = attributes.getRequest();
                // 同步请求头数据，Cookie
                String cookie = request.getHeader("Cookie");
                // 给新请求同步了老请求的cookie
                template.header("Cookie",cookie);

                System.out.println("fegin远程之前先进行RequestInterceptor.apply");
            }
        };
    }
}
