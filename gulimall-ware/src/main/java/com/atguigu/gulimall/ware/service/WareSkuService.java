package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.SpuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
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

    /**
     * @description
     * @param wareSkuLockVo 为某个订单锁定库存
     * @return java.util.List<com.atguigu.gulimall.ware.vo.LockStockResult>
     * @version V1.0.0
     * @date 1:21 下午 2021/10/4
     * @author tangyao
     */
    Boolean orderLockStock(WareSkuLockVo wareSkuLockVo);


}

