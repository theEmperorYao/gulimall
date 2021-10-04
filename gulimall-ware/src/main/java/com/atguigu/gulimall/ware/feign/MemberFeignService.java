package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Classname MemberFeignService
 * @Description TODO
 * @Date 2021/10/2 3:28 下午
 * @Created by tangyao
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {


    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R addrInfo(@PathVariable("id") Long id);
}
