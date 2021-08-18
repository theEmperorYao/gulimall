package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @Classname SkuItemVo
 * @Description TODO
 * @Date 2021/8/17 5:55 下午
 * @Created by tangyao
 */
@Data
public class SkuItemVo {

    /**
     * 基本信息
     */

    SkuInfoEntity info;

    boolean hasStock = true;

    /**
     * 图片信息
     */
    List<SkuImagesEntity> images;

    /**
     * 销售属性组合
     */
    List<ItemSaleAttrVo> saleAttr;

    /**
     * 介绍
     */
    SpuInfoDescEntity desc;

    /**
     * 参数规格信息
     */
    List<SpuItemAttrGroup> groupAttrs;

    /**
     * 秒杀信息
     */
    SeckillInfoVo seckillInfoVo;
}
