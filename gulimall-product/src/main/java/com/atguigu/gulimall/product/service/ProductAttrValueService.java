package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.BaseAttrs;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 00:43:18
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBaseAttrsInfo(Long id, List<BaseAttrs> baseAttrs);

    List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);

    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> productAttrValueEntities);

    List<ProductAttrValueEntity> getAttrsBySpuId(Long spuId);

}

