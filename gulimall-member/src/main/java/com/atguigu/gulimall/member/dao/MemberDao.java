package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 16:48:10
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
