package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Classname FareVo
 * @Description TODO
 * @Date 2021/10/3 2:40 下午
 * @Created by tangyao
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
