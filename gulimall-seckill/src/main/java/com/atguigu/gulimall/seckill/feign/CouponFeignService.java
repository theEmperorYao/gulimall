package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Classname CouponFeignService
 * @Description TODO
 * @Date 2021/10/10 9:03 下午
 * @Created by tangyao
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/lates3DaysSession")
    R getLates3DaysSession();
}
