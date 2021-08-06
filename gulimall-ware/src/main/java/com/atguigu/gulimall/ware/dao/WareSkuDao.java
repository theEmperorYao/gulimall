package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.vo.SpuHasStockVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 * 
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 17:09:23
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("id") Long id, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    List<SpuHasStockVo> getSkuHasStock(List<Long> ids);


    Long getSkuStock(Long skuId);

    @Select("SELECT sku_id, SUM(stock - stock_locked) FROM wms_ware_sku GROUP BY sku_id")
    Map<Long, Long> getSkuStock2();

}
