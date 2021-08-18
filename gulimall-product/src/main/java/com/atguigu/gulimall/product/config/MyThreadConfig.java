package com.atguigu.gulimall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Classname MyThreadConfig
 * @Description TODO
 * @Date 2021/8/18 12:07 上午
 * @Created by tangyao
 */
@Configuration
public class MyThreadConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties threadPoolConfigProperties){

        return new ThreadPoolExecutor(threadPoolConfigProperties.getCoreSize(), threadPoolConfigProperties.getMaxSize(), threadPoolConfigProperties.getKeepAliveTime() , TimeUnit.SECONDS, new LinkedBlockingDeque<>(10000), Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
    }
}
