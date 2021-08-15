package com.atguigu.gulimall.search.vo;

import com.atguigu.gulimall.search.constant.EsConstant;
import lombok.Data;

import java.util.List;

/**
 * @Classname SearchParam
 * @Description TODO
 * @Date 2021/8/11 12:49 下午
 * @Created by tangyao
 */
@Data
public class SearchParam {

    /**
     * 全文检索关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件：sort=price/salecount/hotscore_desc/asc
     */
    private String sort;

    /**
     * 是否有货
     */
    private Integer hasStock ;

    /**
     * 价格查询区间
     */
    private String skuPrice;

    /**
     * 品牌id
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生所有查询属性
     */
    private String _queryString;


}
