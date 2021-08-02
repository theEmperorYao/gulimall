package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrRespVO;
import com.atguigu.gulimall.product.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 00:43:18
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVO attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, String type, Long catelogId);

    AttrRespVO getAttrInfo(Long attrId);

    void updateAttr(AttrRespVO attr);

    PageUtils getOtherAttrs(Map<String, Object> params, int attrgroupId);

    void saveDetail(AttrVO attrVO);

    PageUtils queryBaseAttrsPage(Map<String, Object> params, String type, Long cateLogId);

    AttrRespVO getAttrsInfo(Long attrId);


    PageUtils getOtherAttrs2(Map<String, Object> params, Long attrgroupId);

    /**
     * 在指定的所有属性里面，挑出检索属性
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

