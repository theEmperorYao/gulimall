package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Classname OrderSubmitVo
 * @Description 封装订单提交的数据
 * @Date 2021/10/2 10:11 下午
 * @Created by tangyao
 */
@Data
public class OrderSubmitVo {

    //收货地址id
    private Long addrId;

    private Integer payType;

    //无需提交购买的商品，去购物车再获取一遍
    //优惠发票

    // 防重令牌
    private String orderToken;

    //应付价格，验价
    private BigDecimal payPrice;

    // 订单备注
    private String note;
    // 用户相关信息，直接去session中取出登录的用户


}
