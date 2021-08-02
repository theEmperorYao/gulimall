package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.PurchaseItem;
import com.atguigu.gulimall.ware.vo.MerageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();


        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)

        );

        return new PageUtils(page);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void meragePurchase(MerageVo merageVo) {
        //如果有采购单id就修改 wms_purchase_detail 中的 purchase_id 和status，如果没有就新增，
        Long purchaseId = merageVo.getPurchaseId();
        if (purchaseId == null) {
            //新建一个
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchurseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //确认采购单状态是0或1才可以合并
        PurchaseEntity entity = this.getById(purchaseId);
        if (entity.getStatus() != WareConstant.PurchurseStatusEnum.CREATED.getCode()
                && entity.getStatus() != WareConstant.PurchurseStatusEnum.ASSIGNED.getCode()) {
            return;
        }

        List<Long> items = merageVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            //确定是哪个采购需求的
            purchaseDetailEntity.setId(item);
            //指定相应的采购单id
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);

            purchaseDetailEntity.setStatus(WareConstant.PurchurseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
        //1.确认采购单是新建或已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(purchaseEntity -> {
            if (purchaseEntity.getStatus() == WareConstant.PurchurseStatusEnum.CREATED.getCode() ||
                    purchaseEntity.getStatus() == WareConstant.PurchurseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(purchaseEntity -> {
            purchaseEntity.setStatus(WareConstant.PurchurseStatusEnum.RECEIVED.getCode());
            purchaseEntity.setUpdateTime(new Date());
            return purchaseEntity;
        }).collect(Collectors.toList());
        //2.改变采购单的状态
        this.updateBatchById(collect);
        //3.改变采购项的状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseId(item.getId());
//            detailEntities.forEach(purchaseDetailEntity -> {
//                purchaseDetailEntity.setStatus(WareConstant.PurchurseDetailStatusEnum.BUYING.getCode());
//            });
            //这么做就不会把其他字段也更新了
            List<PurchaseDetailEntity> collect1 = detailEntities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchurseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(collect1);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {


        Long id = purchaseDoneVo.getId();

        //2.改变采购项状态
        boolean flag = true;
        List<PurchaseItem> purchaseItems = purchaseDoneVo.getItems();

        List<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();
        for (PurchaseItem purchaseItem : purchaseItems) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (purchaseItem.getStatus() == WareConstant.PurchurseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
            }
            //3.将成功采购的进行入库
            //哪件商品存起哪件仓库里多少件，分别涉及三个字段，sku_id ware_id stock 应该从采购项wms_purchase_detail中查出来
            PurchaseDetailEntity byId = purchaseDetailService.getById(purchaseItem.getItemId());
            wareSkuService.addStock(byId.getId(),byId.getWareId(),byId.getSkuNum());

            purchaseDetailEntity.setId(purchaseItem.getItemId());
            purchaseDetailEntity.setStatus(purchaseItem.getStatus());
            purchaseDetailEntities.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        //1.改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchurseStatusEnum.FINISHED.getCode() :
                WareConstant.PurchurseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);




    }

}