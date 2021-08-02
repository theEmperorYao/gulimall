package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.PurchurseFeignService;
import com.atguigu.gulimall.ware.vo.SpuHasStockVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    PurchurseFeignService purchurseFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        /**
         *    wareId: 123,//仓库id
         *    skuId: 123//商品id
         */
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(wareId)) {
            wrapper.eq("sku_id", skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long id, Long wareId, Integer skuNum) {
        //1.判断如果没有这个库存记录，就是新增操作
        WareSkuEntity wareSkuEntity = wareSkuDao.selectOne(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", id).eq("ware_id", wareId));
        if (wareSkuEntity == null) {
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(id);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            entity.setStockLocked(0);
            //远程查询sku名字，如果失败整个事务不需要回滚，
            //1.自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                //避免获取商品名字导致事务回滚
                R info = purchurseFeignService.info(id);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map) info.get("skuInfo");
                    String skuName = (String) skuInfo.get("skuName");
                    entity.setSkuName(skuName);
                }
            } catch (Exception e) {

            }

            wareSkuDao.insert(entity);
        } else {
            wareSkuDao.addStock(id, wareId, skuNum);
        }
    }

    @Override
    public List<SpuHasStockVo> getSkuHasStock(List<Long> ids) {
        List<SpuHasStockVo> collect = ids.stream().map(skuId -> {
            SpuHasStockVo spuHasStockVo = new SpuHasStockVo();
            Long count = baseMapper.getSkuStock(skuId);
            spuHasStockVo.setSkuId(skuId);
            spuHasStockVo.setStock(count == null ? false : count > 0);
            return spuHasStockVo;
        }).collect(Collectors.toList());

        return collect;
    }

}