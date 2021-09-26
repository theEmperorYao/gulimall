package com.atguigu.gulimall.cart.service.impl;

import com.atguigu.gulimall.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Classname CartServiceImpl
 * @Description TODO
 * @Date 2021/9/26 5:35 下午
 * @Created by tangyao
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;




}
