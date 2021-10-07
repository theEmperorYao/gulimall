package com.atguigu.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @Classname StockLockTp
 * @Description TODO
 * @Date 2021/10/7 12:19 下午
 * @Created by tangyao
 */
@Data
public class StockLockTo {
    /**
     * 库存工作单id
     */
    private Long id;

    /**
     * 工作单详情
     */
    private StockDetailTo detail;
}
