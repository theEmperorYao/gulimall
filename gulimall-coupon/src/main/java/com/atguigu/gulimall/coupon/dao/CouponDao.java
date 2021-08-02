package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 16:36:50
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
