package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Classname MemberFeignService
 * @Description TODO
 * @Date 2021/9/14 12:25 上午
 * @Created by tangyao
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo userLoginVo);

    @PostMapping("/member/member/oauth/login")
    R oauthLogin(@RequestBody SocialUser socialUser);

}
