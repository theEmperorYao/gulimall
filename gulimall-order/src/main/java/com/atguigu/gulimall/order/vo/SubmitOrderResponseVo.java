package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Classname SubmitOrderResponseVo
 * @Description TODO
 * @Date 2021/10/2 10:57 下午
 * @Created by tangyao
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;
    private Integer code;// 0 成功 错误状态码

}
