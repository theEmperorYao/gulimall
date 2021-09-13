package com.atguigu.gulimall.member.exception;

/**
 * @Classname UserNameExistException
 * @Description TODO
 * @Date 2021/9/13 11:25 上午
 * @Created by tangyao
 */
public class UserNameExistException extends RuntimeException {
    public UserNameExistException() {
        super("用户名存在");
    }

}
