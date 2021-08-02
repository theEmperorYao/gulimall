package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年09月22日 06:59:00
 */
@Data
public class SpuBoundTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}
