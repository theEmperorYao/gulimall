package com.atguigu.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 秒杀活动场次
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-17 16:36:49
 */
@RestController
@RequestMapping("coupon/seckillsession")
public class SeckillSessionController {
    @Autowired
    private SeckillSessionService seckillSessionService;

    @GetMapping("lates3DaysSession")
    public R getLates3DaysSession() {
        List<SeckillSessionEntity> sessions = seckillSessionService.getLates3DaysSession();
        return R.ok().setData(sessions);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:seckillsession:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:seckillsession:info")
    public R info(@PathVariable("id") Long id) {
        SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:seckillsession:save")
    public R save(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:seckillsession:update")
    public R update(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:seckillsession:delete")
    public R delete(@RequestBody Long[] ids) {
        seckillSessionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
