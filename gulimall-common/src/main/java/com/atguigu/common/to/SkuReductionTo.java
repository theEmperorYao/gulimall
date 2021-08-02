package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年09月22日 16:12:00
 */
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;

}
