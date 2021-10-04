package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Classname ProductFeignService
 * @Description TODO
 * @Date 2021/10/3 10:44 下午
 * @Created by tangyao
 */
@FeignClient("gulimall-product")
public interface  ProductFeignService {
    @PostMapping("/product/spuinfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}
