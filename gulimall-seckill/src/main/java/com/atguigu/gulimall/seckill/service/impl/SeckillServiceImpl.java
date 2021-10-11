package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Classname SeckillServiceImpl
 * @Description TODO
 * @Date 2021/10/10 9:01 下午
 * @Created by tangyao
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";// +商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1、扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLates3DaysSession();
        if (session.getCode() == 0) {
            List<SeckillSessionWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionWithSkus>>() {
            });

            //  缓存到redis
            //1、缓存活动信息
            saveSessionInfos(sessionData);

            //2、缓存活动的关联商品信息
            saveSessionSkuInfos(sessionData);

        }

    }

    /**
     * 返回当前时间可以参与秒杀的商品信息
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {

        //1、确定当前时间属于哪个秒杀场次
        // 1970 -
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            //seckill:sessions:1634019458000_1634140800000
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long startTime = Long.parseLong(s[0]);
            long endTime = Long.parseLong(s[1]);
            if (time >= startTime && time <= endTime) {
                //2、获取整个秒杀场次需要的所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (list != null) {
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redisTo = JSON.parseObject((String) item, SeckillSkuRedisTo.class);
//                        redisTo.setRandomCode(null); 当前秒杀开始了，就需要随机码
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }

        }

        //2、获取哪个秒杀场次需要的所有商品信息

        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        // 找到所有需要参与秒杀的商品key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                //6-4
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    // 随机码
                    long current = new Date().getTime();
                    if (current >= skuRedisTo.getStartTime() && current <= skuRedisTo.getEndTime()) {
                        skuRedisTo.setRandomCode(null);
                    }
                    return skuRedisTo;
                }
            }
        }

        return null;
    }

    private void saveSessionInfos(List<SeckillSessionWithSkus> sessionData) {

        sessionData.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();

            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;

            if (redisTemplate.hasKey(key)) {
                return;
            }
            List<String> collect = session.getRelationSkus().stream().map(item ->
                    // 把场次和skuid拼接在一起
                    item.getPromotionSessionId() + "_" + item.getSkuId().toString()
            ).collect(Collectors.toList());
            //缓存活动信息
            redisTemplate.opsForList().leftPushAll(key, collect);
        });

    }

    private void saveSessionSkuInfos(List<SeckillSessionWithSkus> sessionData) {
        sessionData.stream().forEach(session -> {
            // 准备hash操作
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

            session.getRelationSkus().stream().forEach(seckillSkuVo -> {

                String key = seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString();

                if (ops.hasKey(key)) {
                    return;
                }
                //缓存商品
                SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();

                //1、sku的基本数据
                R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());

                if (skuInfo.getCode() == 0) {
                    SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    redisTo.setSkuInfoVo(info);
                }
                //2、sku的秒杀信息
                BeanUtils.copyProperties(seckillSkuVo, redisTo);

                //3、设置上当前商品的秒杀时间信息
                redisTo.setStartTime(session.getStartTime().getTime());
                redisTo.setEndTime(session.getEndTime().getTime());

                //4、商品的随机码
                String token = UUID.randomUUID().toString().replace("-", "");
                redisTo.setRandomCode(token);


                ops.put(key, JSON.toJSONString(redisTo));
                String stockKey = SKU_STOCK_SEMAPHORE + token;

                //如果当前这个场次的商品的库存信息已经上架就不需要上架了
                //5、使用库存作为分布式信号量 限流；
                RSemaphore semaphore = redissonClient.getSemaphore(stockKey);
                //商品可以秒杀的数量作为信号量
                semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());

            });
        });
    }
}
