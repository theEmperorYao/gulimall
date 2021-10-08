package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.config.MyRabbitConfig;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.PurchurseFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.SpuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    PurchurseFeignService purchurseFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    MyRabbitConfig myRabbitConfig;

    @Autowired
    OrderFeignService orderFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        /**
         *    wareId: 123,//仓库id
         *    skuId: 123//商品id
         */
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(wareId)) {
            wrapper.eq("sku_id", skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long id, Long wareId, Integer skuNum) {
        //1.判断如果没有这个库存记录，就是新增操作
        WareSkuEntity wareSkuEntity = wareSkuDao.selectOne(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", id).eq("ware_id", wareId));
        if (wareSkuEntity == null) {
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(id);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            entity.setStockLocked(0);
            //远程查询sku名字，如果失败整个事务不需要回滚，
            //1.自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                //避免获取商品名字导致事务回滚
                R info = purchurseFeignService.info(id);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map) info.get("skuInfo");
                    String skuName = (String) skuInfo.get("skuName");
                    entity.setSkuName(skuName);
                }
            } catch (Exception e) {

            }

            wareSkuDao.insert(entity);
        } else {
            wareSkuDao.addStock(id, wareId, skuNum);
        }
    }

    @Override
    public List<SpuHasStockVo> getSkuHasStock(List<Long> ids) {
        List<SpuHasStockVo> collect = ids.stream().map(skuId -> {
            SpuHasStockVo spuHasStockVo = new SpuHasStockVo();
            Long count = baseMapper.getSkuStock(skuId);
            spuHasStockVo.setSkuId(skuId);
            spuHasStockVo.setStock(count == null ? false : count > 0);
            return spuHasStockVo;
        }).collect(Collectors.toList());

        return collect;
    }

    /**
     * @param vo
     * @return java.lang.Boolean
     * @description 默认只要是运行时异常都会回滚
     * (rollbackFor = NoStockException.class)
     * <p>
     * 库存解锁场景
     * 1）、下订单成功，订单过期没有支付被系统自动取消，被用户手动取消，都要解锁库存
     * <p>
     * 2）、下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     * 之前锁定的库存就要自动解锁。
     * @version V1.0.0
     * @date 2:35 下午 2021/10/4
     * @author tangyao
     */

    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /**
         *  保存库存工作单详情。
         *  追溯。
         */

        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);


        //1、按照下单的收货地址，找到一个就近仓库，锁定库存

        //1、找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            //查询这个商品在哪里有库存
            List<Long> wareIds = baseMapper.listWareIdHasSkuStock(skuId);
            stock.setNum(item.getCount());
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        //2、锁定库存

        for (SkuWareHasStock hasStock : collect) {

            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //没有任何仓库有这个商品的库存
                throw new NoStockException(skuId.toString());
            }
            //1、如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给MQ
            //2、锁定失败。前面保存的工作单信息就回滚了。发送出去的消息，即使要解锁记录，由于去数据库查不到id，所以不用解锁
            // 1:1 -2 -1    2:2 - 1 - 2  3: 3 - 1 - 1
            for (Long wareId : wareIds) {
                // 成功就返回1 否则是0
                Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;

                    // TODO 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity =
                            new WareOrderTaskDetailEntity(
                                    null,
                                    skuId,
                                    "",
                                    hasStock.getNum(),
                                    taskEntity.getId(),
                                    wareId, 1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    StockLockTo lockTo = new StockLockTo();
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    lockTo.setId(taskEntity.getId());
                    // 只发id不行，防止回滚以后找不到数据
                    lockTo.setDetail(stockDetailTo);


                    rabbitTemplate.convertAndSend(myRabbitConfig.getEventExchange(), myRabbitConfig.getRoutingKey(), lockTo);
                    break;
                } else {
                    // 当前仓库锁失败，重试下一个仓库

                }
            }
            if (skuStocked == false) {
                //当前商品所有的仓库都没锁住
                throw new NoStockException(skuId.toString());
            }
        }

        //3、肯定全部都是锁定成功的
        return true;
    }

    /**
     * 1、 库存自动解锁
     * 下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁。
     * 2、订单失败
     * 锁库存失败
     * <p>
     * 只要解锁库存的消息失败。一定要告诉服务器解锁失败
     *
     * @param to
     */
    @Override
    public void unlockStock(StockLockTo to) {


        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        // 解锁
        //1、查询数据库关于这个订单的锁库存消息。
        // 有：证明库存锁定成功
        //    解锁：订单情况
        //    1、没有这个订单。必须解锁
        //    2、有这个订单，不是解锁库存。
        //        订单状态: 已取消：解锁库存
        //                 没取消：不能解锁
        // 没有：库存锁定失败，库存回滚了。这种情况无需解锁
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            //解锁
            // 库存工作单id
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            // 根据订单号查询订单状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                //订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });

                if (data == null || data.getStatus() == 4) {
                    // 订单不存在
                    //订单已经被取消了。才能解锁库存
                    if (byId.getLockStatus() == 1) {
                        // 当前库存工作单详情，状态1，已锁定，但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }

            } else {
                // 订单查询失败，不能消费队列
                // 消息拒绝以后重新放到队列里面，让别人继续消费解锁
                throw new RuntimeException("远程服务失败");
            }

        } else {

        }

    }

    /**
     * 防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期。查订单新建状态，什么都不做就走了。
     * 导致卡顿的订单永远无法解锁库存
     *
     * @param orderTo
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        // 查一下最新库存的状态，防止重复解锁
        WareOrderTaskEntity task = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        // 按照工作单找到所有没有解锁的库存进行解锁
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", id)
                        .eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }

    }

    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        // 库存解锁
        wareSkuDao.unlockStock(skuId, wareId, num);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);//变为已解锁
        wareOrderTaskDetailService.updateById(entity);
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}