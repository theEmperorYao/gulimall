package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.MerageVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 17:09:24
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void meragePurchase(MerageVo merageVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo purchaseDoneVo);

}

