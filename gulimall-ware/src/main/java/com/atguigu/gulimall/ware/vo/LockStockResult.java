package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @Classname LockStockResult
 * @Description TODO
 * @Date 2021/10/4 1:16 下午
 * @Created by tangyao
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
