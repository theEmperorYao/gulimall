package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.BaseAttrs;
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

import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBaseAttrsInfo(Long id, List<BaseAttrs> baseAttrs) {
        List<AttrEntity> attrEntityList = attrService.getBaseMapper().selectList(new QueryWrapper<>());
        //根据id分组，从缓存里面查
        Map<Long, List<AttrEntity>> longListMap =
                attrEntityList.stream().collect(Collectors.groupingBy(AttrEntity::getAttrId));

        if (baseAttrs != null && baseAttrs.size() != 0) {
            List<ProductAttrValueEntity> collect = baseAttrs.stream().map(baseAttr -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setSpuId(id);
                productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                String attrName = longListMap.get(baseAttr.getAttrId()).get(0).getAttrName();
                productAttrValueEntity.setAttrName(attrName);
                productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            this.saveBatch(collect);
        }
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        List<ProductAttrValueEntity> attrValueEntityList =
                this.baseMapper.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        return attrValueEntityList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> productAttrValueEntities) {
        //删除这个spu之前对应的所有属性
        this.baseMapper.delete(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        List<ProductAttrValueEntity> collect = productAttrValueEntities.stream().map(item -> {
            item.setSpuId(spuId);
            return item;
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

    @Override
    public List<ProductAttrValueEntity> getAttrsBySpuId(Long spuId) {
        return this.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
    }

}