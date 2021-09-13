package com.atguigu.gulimall.member.exception;

/**
 * @Classname PhoneExistException
 * @Description TODO
 * @Date 2021/9/13 11:26 上午
 * @Created by tangyao
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
    super("手机号存在");
    }
}
