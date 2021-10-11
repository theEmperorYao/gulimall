package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 17:04:05
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description 订单确认页返回需要的数据
     * @return com.atguigu.gulimall.order.vo.OrderConfirmVo
     * @version V1.0.0
     * @date 8:09 下午 2021/10/1
     * @author tangyao
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;


    /**
     * @description 下单
     * @param orderSubmitVo 
     * @return com.atguigu.gulimall.order.vo.SubmitOrderResponseVo
     * @version V1.0.0
     * @date 11:01 下午 2021/10/2
     * @author tangyao
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

    /**
     * 获取当前订单的支付信息
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);

    void createSecKillOrder(SecKillOrderTo secKillOrder);
}

