package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVO;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 00:43:18
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrEntity> getAttrsRelation(Long attrgroupId);

    List<AttrGroupWithAttrsVO> getAttrgroupWithAttrsByCatelogId(Long catelogId);

    PageUtils queryPageById(Map<String, Object> params, Long catelogId);

    List<Long> findPath(Long catelogId);

    List<AttrEntity> getAttrsRelation2(Long attrGroupId);

    List<AttrGroupWithAttrsVO> getAttrGroupWithAttrs(Long catelogId);

    List<SpuItemAttrGroup> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

