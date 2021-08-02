package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import net.minidev.json.JSONArray;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;


    /**
     * Auto-generated: 2020-10-16 19:45:37
     *
     * @author bejson.com (i@bejson.com)
     * @website http://www.bejson.com/java2pojo/
     */
    @ToString
    @Data
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

    }

    @Test
    void searchData() throws IOException {
        //1.创建一个检索请求
        SearchRequest searchRequest = new SearchRequest();
        //制定索引
        searchRequest.indices("bank");
        //制定DSL，检索条件
        // SearchSourceBuilder searchSourceBuilder 封装检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.1)构造检索条件
//        searchSourceBuilder.query();
//        searchSourceBuilder.from();
//        searchSourceBuilder.size();
//        searchSourceBuilder.aggregation();

        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        //1.2)按照年龄的值分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);

        //1.3)计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);

        System.out.println("检索条件" + searchSourceBuilder.toString());


        SearchRequest source = searchRequest.source(searchSourceBuilder);
        //2.执行检索
        SearchResponse searchResponse = client.search(source, GulimallElasticSearchConfig.COMMON_OPTIONS);


        //3、分析结果
        System.out.println(searchResponse.toString());

//        Map map = JSON.parseObject(searchResponse.toString(), Map.class);
        //3.1)、获取所有查到的数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            //在此之前根据json生成java对象Account
            String sourceAsString = hit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println("account = " + account);
        }

        //3.2)获取检索到的分析信息
        Aggregations aggregations = searchResponse.getAggregations();
//        for (Aggregation aggregation : aggregations.asList()) {
//            System.out.println("name = " + aggregation.getName());
//        }
        System.out.println("aggregations = " + aggregations.toString());
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄 = " + keyAsString + "===>" + bucket.getDocCount());
        }
        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资" + balanceAvg1.getValue());
        System.out.println(aggregations);
    }


    /**
     * 测试存储数据到es
     * 更新也可以
     */
    @Test
    void indexData() throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id("1");
//        request.source("username","zhangsan","age",18,"gender","男");
        User user = new User();
        user.setUserName("zhangsan");
        user.setAge(22);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        request.source(jsonString, XContentType.JSON);
        //网络操作都会有异常，
        //执行操作，
        IndexResponse index = client.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //提取有用的响应数据
        System.out.println(index);
    }

    @Data
    class User {
        private String userName;
        private String gender;
        private Integer age;
    }


    @Test
    void contextLoads() {
        System.out.println(client);
    }

}
