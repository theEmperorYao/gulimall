package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVO;
import com.atguigu.gulimall.product.vo.AttrRespVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationDao relationDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void deleteRelation(AttrGroupRelationVO[] relationVOS) {
        List<AttrAttrgroupRelationEntity> relationEntities = Arrays.stream(relationVOS).map(relationVO -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(relationVO, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        //delete from pms_attr_attrgroup_relation where (attr_id=? and attr_group_id=?) or (attr_id=? and attr_group_id=?)
        baseMapper.deleteBatchRelation(relationEntities);
    }

    @Override
    public void setRelation(List<AttrRespVO> attrRespVOS) {
        List<AttrAttrgroupRelationEntity> list=new ArrayList<>();
//        attrRespVOS.stream().forEach(item->{
//            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
//            BeanUtils.copyProperties(item,relationEntity);
//            list.add(relationEntity);
//        });
        List<AttrAttrgroupRelationEntity> collect = attrRespVOS.stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

    @Override
    public void deleteRelation2(List<AttrGroupRelationVO> relationVOS) {
        List<AttrAttrgroupRelationEntity> collect = relationVOS.stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());

        relationDao.deleteBatchRelation2(collect);
    }


}