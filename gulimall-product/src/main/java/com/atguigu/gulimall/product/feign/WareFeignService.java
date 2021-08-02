package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.vo.ware.SpuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月17日 12:00:00
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 1、R 在设计的时候加上泛型
     * 2、直接返回我们想要的结果
     * 3、自己封装解析结果
     * @param ids
     * @return
     */
    @PostMapping("ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> ids);




}
