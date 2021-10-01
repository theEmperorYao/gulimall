package com.atguigu.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 16:48:10
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description
     * @param memberId 
     * @return java.util.List<com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity>
     * @version V1.0.0
     * @date 8:14 下午 2021/10/1
     * @author tangyao
     */
    List<MemberReceiveAddressEntity> getAddress(Long memberId);
}

