package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.PurchurseFeignService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.SpuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
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
import org.springframework.transaction.annotation.Transactional;


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

    /**
     * @description
     * 默认只要是运行时异常都会回滚
     * (rollbackFor = NoStockException.class)
     * @param vo 
     * @return java.lang.Boolean
     * @version V1.0.0
     * @date 2:35 下午 2021/10/4
     * @author tangyao
     */

    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        //1、按照下单的收货地址，找到一个就近仓库，锁定库存

        //1、找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            //查询这个商品在哪里有库存
            List<Long> wareIds = baseMapper.listWareIdHasSkuStock(skuId);
            stock.setNum(item.getCount());
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        //2、锁定库存

        for (SkuWareHasStock hasStock : collect) {

            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //没有任何仓库有这个商品的库存
                throw new NoStockException(skuId.toString());
            }
            for (Long wareId : wareIds) {
                // 成功就返回1 否则是0
                Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    break;
                } else {
                    // 当前仓库锁失败，重试下一个仓库

                }
            }
            if (skuStocked == false) {
                //当前商品所有的仓库都没锁住
                throw new NoStockException(skuId.toString());
            }
        }

        //3、肯定全部都是锁定成功的
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}