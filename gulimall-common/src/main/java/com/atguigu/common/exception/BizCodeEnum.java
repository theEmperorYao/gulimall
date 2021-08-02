package com.atguigu.common.exception;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年08月26日 23:47:00
 */
public enum BizCodeEnum {
    //
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架失败");
    private int code;
    private String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
