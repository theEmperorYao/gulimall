package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Classname OrderItemVo
 * @Description TODO
 * @Date 2021/10/1 8:02 下午
 * @Created by tangyao
 */
@Data
public class OrderItemVo {

    private Long skuId;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    // todo 查询库存状态
    private boolean stocks;

    private BigDecimal weight;


}
