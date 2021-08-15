package com.atguigu.common.to.es;

import jdk.internal.util.xml.impl.Attrs;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description
 * @createTime 2020年10月17日 08:54:00
 */

@Data
public class SkuEsModel {
    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private Boolean hasStock;

    private Long hotScore;

    private Long brandId;

    private String brandName;

    private String brandImg;

    private Long catalogId;

    private String catalogName;


    private List<Attrs> attrs;

    @Data
    //为了第三方工具能对它序列化反序列化，设置为可访问的权限
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }

}
