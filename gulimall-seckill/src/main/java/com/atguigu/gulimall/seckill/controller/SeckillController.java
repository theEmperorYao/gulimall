package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Classname seckillController
 * @Description TODO
 * @Date 2021/10/11 1:46 下午
 * @Created by tangyao
 */
@Slf4j
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 返回当前时间可以参与秒杀的商品信息
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {

        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {

        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }


    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam(value = "num") Integer num,
                          Model model) {

        long s1 = System.currentTimeMillis();

        String orderSn = seckillService.kill(killId, key, num);
        long s2 = System.currentTimeMillis();
        log.info("耗时" + (s2 - s1));
        model.addAttribute("orderSn", orderSn);
        //1、判断是否登录

        return "success";
    }

}
