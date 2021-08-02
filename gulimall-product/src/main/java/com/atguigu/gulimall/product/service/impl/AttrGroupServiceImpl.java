package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private AttrDao attrDao;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        //select * from pms_attr_group where catelog_id=? and (attr_group_id=key or attr_group_name like key)
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(obj -> obj
                    .eq("attr_group_id", key)
                    .or()
                    .like("attr_group_name ", key));
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrEntity> getAttrsRelation(Long attrgroupId) {

        List<AttrAttrgroupRelationEntity> relationEntities =
                attrAttrgroupRelationDao.selectList(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrIds =
                relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        if (attrIds != null && !attrIds.isEmpty()) {
            return attrDao.selectBatchIds(attrIds);
        }
        return new ArrayList<>();
    }

    @Override
    public List<AttrGroupWithAttrsVO> getAttrgroupWithAttrsByCatelogId(Long catelogId) {

        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVO> collect = groupEntities.stream().map(group -> {
            AttrGroupWithAttrsVO attrGroupWithAttrsVO = new AttrGroupWithAttrsVO();
            BeanUtils.copyProperties(group, attrGroupWithAttrsVO);
            List<AttrEntity> attrs = attrGroupService.getAttrsRelation(attrGroupWithAttrsVO.getAttrGroupId());
            attrGroupWithAttrsVO.setAttrs(attrs);
            return attrGroupWithAttrsVO;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public PageUtils queryPageById(Map<String, Object> params, Long catelogId) {

        QueryWrapper<AttrGroupEntity> wrapper =
                new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(w -> {
                w.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<Long> findPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        path.add(catelogId);

        while (categoryEntity.getParentCid() != 0) {
            path.add(categoryEntity.getParentCid());
            categoryEntity = categoryService.getById(categoryEntity.getParentCid());
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public List<AttrEntity> getAttrsRelation2(Long attrGroupId) {

        List<AttrAttrgroupRelationEntity> relationEntities =
                attrAttrgroupRelationDao.selectList(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));

        List<AttrEntity> collect = relationEntities.stream().map(item -> {
            Long attrId = item.getAttrId();
            AttrEntity attrEntity = attrService.getById(attrId);
            return attrEntity;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<AttrGroupWithAttrsVO> getAttrGroupWithAttrs(Long catelogId) {
        List<AttrGroupEntity> groupEntities = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVO> groupWithAttrsVOS = groupEntities.stream().map(item -> {
            AttrGroupWithAttrsVO attrGroupWithAttrsVO = new AttrGroupWithAttrsVO();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVO);
            List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",
                    item.getAttrGroupId()));
            List<Long> attrIds =
                    relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            List<AttrEntity> attrEntities = attrService.getBaseMapper().selectBatchIds(attrIds);
            if (attrEntities!=null&&attrEntities.size()!=0){
                attrGroupWithAttrsVO.setAttrs(attrEntities);
            }else {
                attrGroupWithAttrsVO.setAttrs(new ArrayList<>());
            }
            return attrGroupWithAttrsVO;
        }).collect(Collectors.toList());
        return groupWithAttrsVOS;
    }


}