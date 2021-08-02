package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.BrandVO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private CategoryService categoryService;

    @Resource
    private BrandService brandService;

    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    private BrandDao brandDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        BrandEntity brandEntity = brandService.getById(brandId);
        String brandEntityName = brandEntity.getName();

        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        String categoryEntityName = categoryEntity.getName();

        categoryBrandRelation.setBrandName(brandEntityName);
        categoryBrandRelation.setCatelogName(categoryEntityName);
//        this.save(categoryBrandRelation);
        baseMapper.insert(categoryBrandRelation);
    }


    @Override
    public void updatebrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        this.update(categoryBrandRelationEntity, new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",
                brandId));
        //其他更新关联
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> list = categoryBrandRelationDao.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<Long> collect = list.stream().map(CategoryBrandRelationEntity::getBrandId).collect(Collectors.toList());
        List<BrandEntity> brandEntities = brandService.getBaseMapper().selectBatchIds(collect);
//        brandDao.selectBatchIds(collect);
        return brandEntities;
    }

    @Override
    public void saveInfo(CategoryBrandRelationEntity categoryBrandRelationEntity) {
        Long brandId = categoryBrandRelationEntity.getBrandId();
        BrandEntity brandEntity = brandService.getById(brandId);
        categoryBrandRelationEntity.setBrandName(brandEntity.getName());

        Long catelogId = categoryBrandRelationEntity.getCatelogId();
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        categoryBrandRelationEntity.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelationEntity);
    }

    @Override
    public List<BrandVO> brandList(Long catId) {
        List<CategoryBrandRelationEntity> relationEntities =
                categoryBrandRelationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<BrandVO> collect = relationEntities.stream().map(item -> {
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandId(item.getBrandId());
            brandVO.setBrandName(item.getBrandName());
            return brandVO;
        }).collect(Collectors.toList());
        return collect;
    }

}