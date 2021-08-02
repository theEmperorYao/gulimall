package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.web.Catalog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 00:43:18
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    /**
     * 找到catelogId的完整路径；
     * [父/子/孙]
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    /**
     * 级联更新
     * @param category
     */
    void updateCascade(CategoryEntity category);

    /**
     * 获取所有分类及子分类
     * @return
     */
    List<CategoryEntity> listShowWithTree();


    List<CategoryEntity> getLevel1Categorys();


    Map<String, List<Catalog2Vo>> getCatalogJson();

}

