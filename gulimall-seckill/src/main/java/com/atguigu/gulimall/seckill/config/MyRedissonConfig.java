package com.atguigu.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月21日 22:49:00
 */
@Configuration
public class MyRedissonConfig {

    /**
     * 所有对Redisson的使用都是对RedissionClient对象的操作
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        //1、创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://172.16.167.2:6379");
        //2、根据Config创建出RedisClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
