package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Classname OrderConfrimVo
 * @Description 订单确认页需要的数据
 * @Date 2021/10/1 6:31 下午
 * @Created by tangyao
 */
@Data
public class OrderConfirmVo {
    //ums_member_receive_address

    List<MemberAddressVo> address;

    // 所有选中的购物项
    List<OrderItemVo> items;

    // 发票记录

    // 优惠券信息
    Integer integration;

    Map<Long,Boolean> stocks;
    // 订单总额
//    BigDecimal total;

    // 应付价格
//    BigDecimal payPrice;

    // 防重令牌
    String orderToken;

    public Integer getCount() {
        Integer sum = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                sum += item.getCount();
            }
        }
        return sum;
    }


    public BigDecimal getTotal() {

        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal count = new BigDecimal(item.getCount().toString());
                BigDecimal multiply = item.getPrice().multiply(count);
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice() {

        return getTotal();
    }
}
