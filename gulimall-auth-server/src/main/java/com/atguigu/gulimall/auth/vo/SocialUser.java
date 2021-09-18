package com.atguigu.gulimall.auth.vo;

/**
 * @Classname SocialUser
 * @Description TODO
 * @Date 2021/9/17 4:21 下午
 * @Created by tangyao
 */

import lombok.Data;

/**
 * Copyright 2021 json.cn
 */
@Data
public class SocialUser {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
    private String scope;
    private Long createdAt;
    private String id;

}
