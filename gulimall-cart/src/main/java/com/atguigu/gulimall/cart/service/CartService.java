package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
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
     * @param skuId
     * @param num
     * @return com.atguigu.gulimall.cart.vo.CartItem
     * @description 将商品添加到购物车
     * @version V1.0.0
     * @date 12:22 上午 2021/9/28
     * @author tangyao
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * @param skuId
     * @return com.atguigu.gulimall.cart.vo.CartItem
     * @description 获取购物车中某个购物项
     * @version V1.0.0
     * @date 12:21 上午 2021/9/28
     * @author tangyao
     */
    CartItem getCartItem(Long skuId);

    /**
     * @return com.atguigu.gulimall.cart.vo.Cart
     * @description 获取整个购物车
     * @version V1.0.0
     * @date 3:26 下午 2021/9/29
     * @author tangyao
     */
    Cart getCart() throws ExecutionException, InterruptedException;


    /**
     * @param cartKey
     * @return void
     * @description 清空购物车数据
     * @version V1.0.0
     * @date 3:58 下午 2021/9/29
     * @author tangyao
     */
    void clearCart(String cartKey);

    /**
     * @description 勾选购物项
     * @param skuId
     * @param check
     * @return void
     * @version V1.0.0
     * @date 7:08 下午 2021/9/29
     * @author tangyao
     */
    void checkItem(Long skuId, Integer check);

    /**
     * @description 修改购物项数量
     * @param skuId
     * @param num
     * @return void
     * @version V1.0.0
     * @date 7:22 下午 2021/9/29
     * @author tangyao
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * @description 删除购物项
     * @param skuId
     * @return void
     * @version V1.0.0
     * @date 7:30 下午 2021/9/29
     * @author tangyao
     */
    void deleteItem(Long skuId);

}
