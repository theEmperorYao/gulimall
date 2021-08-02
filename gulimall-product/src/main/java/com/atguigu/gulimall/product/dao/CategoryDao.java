package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品三级分类
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 00:43:18
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {

    /**
     * 同步数据
     *
     * @param catId
     * @param name
     */
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);

}
