package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年09月26日 13:48:00
 */
@Data
public class PurchaseItem {

    private Long itemId;
    private Integer status;
    private String reason;
}
