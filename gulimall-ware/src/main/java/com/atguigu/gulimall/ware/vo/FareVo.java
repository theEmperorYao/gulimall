package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Classname FareVo
 * @Description TODO
 * @Date 2021/10/2 3:53 下午
 * @Created by tangyao
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
