package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @Classname ItemSaleAttrVo
 * @Description TODO
 * @Date 2021/8/17 5:56 下午
 * @Created by tangyao
 */
@Data
public class ItemSaleAttrVo{
    private Long attrId;

    private String attrName;

    private List<AttrValueWithSkuIdVo> attrValues;
}
