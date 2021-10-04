package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Classname OrderCreateTo
 * @Description TODO
 * @Date 2021/10/3 2:20 下午
 * @Created by tangyao
 */
@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;// 订单计算的应付价格

    private BigDecimal fare;//运费


}
