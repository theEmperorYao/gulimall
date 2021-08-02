package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 17:04:05
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
