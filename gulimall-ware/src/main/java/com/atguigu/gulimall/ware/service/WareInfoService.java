package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.FareVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 17:09:24
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description 根据用户的收货地址计算运费
     * @param addrId
     * @return java.math.BigDecimal
     * @version V1.0.0
     * @date 3:26 下午 2021/10/2
     * @author tangyao
     */
    FareVo getFare(Long addrId);
    
}

