package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname SearchResult
 * @Description TODO
 * @Date 2021/8/11 2:05 下午
 * @Created by tangyao
 */
@Data
public class SearchResult {

    /**
     * 商品信息
     */
    private List<SkuEsModel> product;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    /**
     * 导航页码
     */
    private List<Integer> pageNavs;

    /**
     * 查询到的所有品牌
     */
    private List<BrandVo> brands;

    /**
     * 查询到的所有分类
     */
    private List<CatalogVo> catalogs;

    /**
     * 查询到的所有品牌
     */
    private List<AttrVo> attrVos;


    //===========================以上是返回给页面的所有信息============================//


    // 面包屑导航数据
    private List<NavVo> navs = new ArrayList<>();

    /**
     * 便于判断当前id是否被使用
     */
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String name;

        private String navValue;

        private String link;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
