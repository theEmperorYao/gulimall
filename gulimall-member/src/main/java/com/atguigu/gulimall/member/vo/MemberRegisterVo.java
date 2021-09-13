package com.atguigu.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Classname MemberRegisterVo
 * @Description TODO
 * @Date 2021/9/13 11:07 上午
 * @Created by tangyao
 */
@Data
public class MemberRegisterVo {

    private String userName;
    private String password;
    private String phone;

}
