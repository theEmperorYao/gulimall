package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.BrandVo;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname MallSearchServiceImpl
 * @Description TODO
 * @Date 2021/8/11 1:29 下午
 * @Created by tangyao
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam searchParam) {

        // 1.动态构建出查询需要的DSL语句
        SearchResult result = null;

        // 1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);

        try {
            // 3.执行检所请求
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            //4.分析响应数据封装成我们需要的格式
            result = buildSearchResult(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response, SearchParam searchParam) {

        SearchResult searchResult = new SearchResult();

        SearchHits hits = response.getHits();

        if (hits.getHits() != null && hits.getHits().length > 0) {
            // 商品信息
            List<SkuEsModel> skuEsModels = Arrays.stream(hits.getHits()).map(hit -> {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                String keyword = searchParam.getKeyword();
                if (StringUtils.isNotEmpty(keyword)) {
                    HighlightField highlightField = hit.getHighlightFields().get("skuTitle");
                    String skuTitle = highlightField.getFragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitle);
                }
                return skuEsModel;
            }).collect(Collectors.toList());
            searchResult.setProduct(skuEsModels);

        }

        //分页信息 - 当前页码
        searchResult.setPageNum(searchParam.getPageNum());
        // 分页信息 - 总记录数
        long totalHits = hits.getTotalHits().value;
        searchResult.setTotal(totalHits);
        // 分页信息 - 总页码 计算
        int totalPage = (int) (totalHits % EsConstant.PRODUCT_PAGESIZE == 0
                ? totalHits / EsConstant.PRODUCT_PAGESIZE
                : totalHits / EsConstant.PRODUCT_PAGESIZE + 1);
        searchResult.setTotalPages(totalPage);

        // 设置导航页码
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        //查询到的所有聚合信息
        Aggregations aggregations = response.getAggregations();
        //查询到的所有品牌
        ParsedLongTerms brand_agg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> buckets = brand_agg.getBuckets();

        List<SearchResult.BrandVo> brandVos = buckets.stream().map(bucket -> {

            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            Long brandId = bucket.getKeyAsNumber().longValue();
            //品牌id
            brandVo.setBrandId(brandId);
            Aggregations bucketAggregations = bucket.getAggregations();
            // 品牌图片
            String imgUrl = ((ParsedStringTerms) bucketAggregations.get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(imgUrl);
            // 品牌名字
            String brandName = ((ParsedStringTerms) bucketAggregations.get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            return brandVo;
        }).collect(Collectors.toList());
        searchResult.setBrands(brandVos);

        //查询到的所有分类
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");

        List<? extends Terms.Bucket> catalog_aggBuckets = catalog_agg.getBuckets();

        List<SearchResult.CatalogVo> catalogVos = catalog_aggBuckets.stream().map(catalog -> {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(catalog.getKeyAsNumber().longValue());
            String catalogName =
                    ((ParsedStringTerms) catalog.getAggregations().get("catalog_name_agg")).getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            return catalogVo;
        }).collect(Collectors.toList());
        searchResult.setCatalogs(catalogVos);

        // 查询所有属性聚合
        ParsedNested attr_agg = aggregations.get("attr_agg");

        List<? extends Terms.Bucket> attrIdAggBucket =
                ((ParsedLongTerms) attr_agg.getAggregations().get("attr_id_agg")).getBuckets();
        List<SearchResult.AttrVo> attrVos = attrIdAggBucket.stream().map(attrAgg -> {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 属性id
            attrVo.setAttrId(attrAgg.getKeyAsNumber().longValue());
            Aggregations subAttrAgg = attrAgg.getAggregations();
            //属性名字
            String attrName = ((ParsedStringTerms) subAttrAgg.get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            //属性值
            List<String> attrValues = ((ParsedStringTerms) subAttrAgg.get("attr_value_agg")).getBuckets()
                    .stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            return attrVo;
        }).collect(Collectors.toList());
        searchResult.setAttrVos(attrVos);

        // 6.构建面包屑导航功能
        if (searchParam.getAttrs() != null) {
            List<SearchResult.NavVo> navVos = searchParam.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.getAttrsInfo(Long.parseLong(s[0]));
                // 将已选择的请求参数添加进去 前端页面进行排除
                searchResult.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr",new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setName(data.getAttrName());
                } else {
                    // 失败了就拿id作为名字
                    navVo.setName(s[0]);
                }
                // 拿到所有查询条件 替换查询条件
                String replace = replaceQueryString(searchParam, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(navVos);
        }

        // 品牌、分类
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = searchResult.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setName("品牌");
            // TODO 远程查询所有品牌
            R r = productFeignService.brandInfo(searchParam.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brand = r.getData("data", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                // 替换所有品牌ID
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName() + ";");
                    replace = replaceQueryString(searchParam, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }


        return searchResult;
    }

    /**
     * 替换字符
     * key ：需要替换的key
     */
    private String replaceQueryString(SearchParam Param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            // 浏览器对空格的编码和java的不一样
            encode = encode.replace("+", "%20");
            encode = encode.replace("%28", "(").replace("%29", ")");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Param.get_queryString().replace("&" + key + "=" + encode, "");
    }


    private SearchRequest buildSearchRequest(SearchParam searchParam) {

        // 1.构建 bool query
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1 must
        String keyword = searchParam.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", keyword));
        }

        //1.2 bool filter
        //bool filter --> 三级分类
        if (searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }

        //bool filterbool --> 品牌id
        List<Long> brandIds = searchParam.getBrandId();
        if (brandIds != null && brandIds.size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandIds));
        }

        //bool filter  --> 指定属性
        //attrs=1_5寸:8寸&2_16G:8G
        List<String> attrs = searchParam.getAttrs();
        if (attrs != null && attrs.size() >= 0) {

            for (String attrStr : attrs) {
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                BoolQueryBuilder nextBoolQueryBuilder = QueryBuilders.boolQuery();
                nextBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nextBoolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nextBoolQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }

        }

        //bool filter --> 库存

        if (searchParam.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }


        //bool filter --> 金额范围 1_500/1_/_500
        String skuPrice = searchParam.getSkuPrice();
        if (StringUtils.isNotEmpty(skuPrice)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] s = skuPrice.split("_");
            if (skuPrice.startsWith("_")) {
                rangeQueryBuilder.lte(s[1]);
            } else if (skuPrice.endsWith("_")) {
                rangeQueryBuilder.gte(s[0]);
            } else {
                rangeQueryBuilder.gte(s[0]).lte(s[1]);
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        // 所有条件拿来封装
        searchSourceBuilder.query(boolQueryBuilder);

        //排序、
        //sort = hotScore_asc/desc
        String sort = searchParam.getSort();
        if (StringUtils.isNotEmpty(sort)) {
            String[] s = sort.split("_");
            searchSourceBuilder.sort(s[0], SortOrder.fromString(s[1]));
        }

        //分页
        // pageNum 1 from 0 size 5 [0,1,2,3,4]
        // pageNum 2 from 5 size 5 [5,6,7,8,9]
        // from = (pageNum -  1 )* size
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //高亮

        if (StringUtils.isNotEmpty(keyword)) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }


        // 聚合
        // 1.品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 1.1. 品牌子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(50));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(50));
        searchSourceBuilder.aggregation(brand_agg);

        // 2.分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        //分类名字聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);
        //3.属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");

        // 聚合当前所有attrid
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        // 聚合当前所有attrid对应名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(10));
        // 聚合当前所有attrid所有可能取
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        attr_agg.subAggregation(attr_id_agg);

        searchSourceBuilder.aggregation(attr_agg);


        System.out.println("构建的dsl语句 searchSourceBuilder = " + searchSourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }
}
