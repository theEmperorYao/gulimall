package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月17日 14:28:00
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        // 保存到es
        //1、给es建立索引 product，由于索引经常用，所以也应该抽取为一个常量
        //建立好映射关系

        //2、给es中保存这些数据，
        BulkRequest bulkRequest = new BulkRequest();
        skuEsModels.forEach(skuEsModel -> {
            //1、构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            //每一条数据都有一条唯一id
            indexRequest.id(skuEsModel.getSkuId().toString());
            //数据内容
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        });

        //批量数据的返回每一条都是独立统计的
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item->item.getId()).collect(Collectors.toList());
        log.info("商品上架成功：{} 返回数据为{}", collect,bulk.toString());
        return b;

    }
}
