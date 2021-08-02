package com.atguigu.gulimall.product.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description 2级分类vo
 * @createTime 2020年10月18日 20:52:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catalog2Vo {
    /**
     * 一级父分类
     */
    private String catalog1Id;
    /**
     * 三级子分类
     */
    private List<Catalog3Vo> catalog3List;
    private String id;
    private String name;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catalog3Vo {
        /**
         * 父分类，2级分类id
         */
        private String catalog2Id;
        private String id;
        private String name;
    }
}
