package com.atguigu.common.exception;

/**
 * @Classname NoStockException
 * @Description TODO
 * @Date 2021/10/4 2:12 下午
 * @Created by tangyao
 */
public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(String msg) {
        super( msg + "号商品没有足够的库存了");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
