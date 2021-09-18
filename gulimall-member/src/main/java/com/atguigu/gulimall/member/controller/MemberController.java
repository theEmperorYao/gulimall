package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;


import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 会员
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 16:48:10
 */

@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;


    @RequestMapping("coupon/list")
    public R couponlist() {

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("帅哥");
        R memberCoupon = couponFeignService.memberCoupon();
        return R.ok().put("member", memberEntity).put("memberCoupon", memberCoupon.get("memberCoupon"));
    }

    @PostMapping("/oauth/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) {
        MemberEntity memberEntity = null;
        try {
            memberEntity = memberService.login(socialUser);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (memberEntity != null) {
            //todo 1、登录后处理
            return R.ok().setData(memberEntity);
        } else {
            BizCodeEnum exception = BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION;
            return R.error(exception.getCode(), exception.getMessage());
        }

    }



    @PostMapping("login")
    public R login(@RequestBody MemberLoginVo memberLoginVo) {
        MemberEntity memberEntity = memberService.login(memberLoginVo);

        if (memberEntity != null) {
            //todo 1、登录后处理
            return R.ok();
        } else {
            BizCodeEnum exception = BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION;
            return R.error(exception.getCode(), exception.getMessage());
        }

    }


    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo memberRegisterVo) {

        try {
            memberService.register(memberRegisterVo);
        } catch (PhoneExistException e) {
            BizCodeEnum phoneExistException = BizCodeEnum.PHONE_EXIST_EXCEPTION;
            return R.error(phoneExistException.getCode(), phoneExistException.getMessage());
        } catch (UserNameExistException e) {
            BizCodeEnum userExistException = BizCodeEnum.USER_EXIST_EXCEPTION;
            return R.error(userExistException.getCode(), userExistException.getMessage());
        }

        return R.ok();
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
