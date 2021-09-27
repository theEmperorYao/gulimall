package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @Classname CartService
 * @Description TODO
 * @Date 2021/9/26 5:34 下午
 * @Created by tangyao
 */
public interface CartService {

    /**
     * @description 将商品添加到购物车
     * @param skuId 
     * @param num
     * @return com.atguigu.gulimall.cart.vo.CartItem
     * @version V1.0.0
     * @date 12:22 上午 2021/9/28
     * @author tangyao
     */
    CartItem addToCart(Long skuId , Integer num) throws ExecutionException, InterruptedException;

    /**
     * @description 获取购物车中某个购物项
     * @param skuId
     * @return com.atguigu.gulimall.cart.vo.CartItem
     * @version V1.0.0
     * @date 12:21 上午 2021/9/28
     * @author tangyao
     */
    CartItem getCartItem(Long skuId);

}
