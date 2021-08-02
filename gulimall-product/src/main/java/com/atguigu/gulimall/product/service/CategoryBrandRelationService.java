package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.vo.BrandVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 00:43:18
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updatebrand(Long brandId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);

    void saveInfo(CategoryBrandRelationEntity categoryBrandRelationEntity);

    List<BrandVO> brandList(Long catId);

}

