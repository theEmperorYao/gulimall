package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCaching
//就算不标注，也能扫描到，带有@FeignClient的接口，前提是主配置类是一个父包，其他都是子包，那就能扫描到子包里面内容
//如果自己额外写了配置类，父子不同包，就一定要显示的声明
@EnableFeignClients(basePackages = {"com.atguigu.gulimall.product.feign"})
@EnableDiscoveryClient
//告诉MyBatisPlusMapper接口都在哪里呢
@MapperScan("com.atguigu.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
