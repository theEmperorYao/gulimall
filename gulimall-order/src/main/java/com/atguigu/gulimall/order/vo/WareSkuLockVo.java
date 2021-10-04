package com.atguigu.gulimall.order.vo;

import lombok.Data;

import javax.swing.plaf.BorderUIResource;
import java.util.List;

/**
 * @Classname WareSkuLockVo
 * @Description TODO
 * @Date 2021/10/4 1:02 下午
 * @Created by tangyao
 */
@Data
public class WareSkuLockVo {
    private String orderSn;// 订单号
    private List<OrderItemVo> locks;//需要锁住得所有库存信息

}
