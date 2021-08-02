package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.SpuHasStockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 17:09:23
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long id, Long wareId, Integer skuNum);

    List<SpuHasStockVo> getSkuHasStock(List<Long> ids);

}

