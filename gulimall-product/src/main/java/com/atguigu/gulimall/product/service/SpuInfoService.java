package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SpuSaveVo;
import com.atguigu.gulimall.product.vo.generated.SpuSaveVo2;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 00:43:18
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    PageUtils queryPageByCondiction(Map<String, Object> params);

    void saveSpuInfo2(SpuSaveVo2 vo);

    /**
     * 商品上架
     * @param spuId
     */
    void up(Long spuId);

    /**
     * @description
     * @param skuId 
     * @return com.atguigu.gulimall.product.entity.SpuInfoEntity
     * @version V1.0.0
     * @date 10:40 下午 2021/10/3
     * @author tangyao
     */
    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}



