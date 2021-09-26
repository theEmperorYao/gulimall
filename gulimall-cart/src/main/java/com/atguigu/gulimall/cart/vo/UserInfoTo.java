package com.atguigu.gulimall.cart.vo;

import lombok.Data;

/**
 * @Classname UserInfoVo
 * @Description TODO
 * @Date 2021/9/26 7:05 下午
 * @Created by tangyao
 */
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey; // 一定封装
    private Boolean tempUser = false;
}
