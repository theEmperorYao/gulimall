package com.atguigu.common.to.mq;

import lombok.Data;

/**
 * @Classname StockDetailTo
 * @Description TODO
 * @Date 2021/10/7 12:34 下午
 * @Created by tangyao
 */
@Data
public class StockDetailTo {
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    /**
     * 仓库id
     */
    private Long wareId;

    /**
     * 锁定文件
     * 1-锁定 2-解锁 3-扣减
     */
    private Integer lockStatus;
}
