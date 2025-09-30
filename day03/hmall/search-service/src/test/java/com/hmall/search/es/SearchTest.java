package com.hmall.search.es;

import cn.hutool.json.JSONUtil;
import com.hmall.search.domain.po.ItemDoc;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;


public class SearchTest {
    private RestHighLevelClient client;
    private static final String INDEX_NAME = "items";
    @BeforeEach
    public void init(){
        client = new RestHighLevelClient(RestClient.builder( HttpHost.create("http://192.168.12.168:9200")));
    }

    @AfterEach
    public void close(){
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleResponse(SearchResponse response) {
        //4.解析响应结果
        SearchHits responseHits = response.getHits();
        long value = responseHits.getTotalHits() != null ? responseHits.getTotalHits().value : 0;
        System.out.println("本次搜索命中的总文档数"+value);
        float maxScore = responseHits.getMaxScore();
        System.out.println("最大得分"+maxScore);
        SearchHit[] hits = responseHits.getHits();
        if (hits != null && hits.length > 0) {
            for (SearchHit hit : hits) {
                ItemDoc itemDoc = JSONUtil.toBean(hit.getSourceAsString(), ItemDoc.class);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields != null && highlightFields.containsKey("name")) {
                    HighlightField highlightField = highlightFields.get("name");
                    String highlightName = highlightField.fragments()[0].string();
                    itemDoc.setName(highlightName);
                }
                System.out.println(itemDoc);
            }
        }
    }

    //测试match_all
    @Test
    public void testMatchAll() throws Exception {
        //1.创建请求对象
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //2.设置请求参数
        searchRequest.source().query(QueryBuilders.matchAllQuery());
        //3.发送请求并获取结果
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        //4.解析响应结果
        handleResponse(response);

    }



    //测试match
    @Test
    public void testMatch() throws Exception {
        //1.创建请求对象
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //2.设置请求参数
        searchRequest.source().query(QueryBuilders.matchQuery("name", "小米"));
        //3.发送请求并获取结果
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        //4.解析响应结果
        handleResponse(response);
    }

    //测试multi_match
    @Test
    public void testMultiMatch() throws Exception {
        //1.创建请求对象
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //2.设置请求参数
        searchRequest.source().query(QueryBuilders.multiMatchQuery("脱脂牛奶", "name", "category"));
        //3.发送请求并获取结果
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        //4.解析响应结果
        handleResponse(response);
    }

    //测试term
    @Test
    public void testTerm() throws Exception {
        //1.创建请求对象
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //2.设置请求参数
        searchRequest.source().query(QueryBuilders.termQuery("category", "牛奶"));
        //3.发送请求并获取结果
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        //4.解析响应结果
        handleResponse(response);
    }

    //测试range
    @Test
    public void testRange() throws Exception {
        //1.创建请求对象
        SearchRequest search = new SearchRequest(INDEX_NAME);
        //2.设置请求参数
        search.source().query(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //3.发送请求并获取结果
        SearchResponse response = client.search(search, RequestOptions.DEFAULT);
        //4.解析响应结果
        handleResponse(response);
    }

    //测试boolQuery
    @Test
    public void testBoolQuery() throws Exception {
        //1.创建请求对象
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //2.设置请求参数
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        boolQueryBuilder.must(QueryBuilders.matchQuery("name","手机"))
//                .filter(QueryBuilders.rangeQuery("price").lte(30000))
//                .filter(QueryBuilders.termQuery("brand","华为"));
//        searchRequest.source().query(boolQueryBuilder);
        searchRequest.source().query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("name","手机"))
                .filter(QueryBuilders.rangeQuery("price").lte(30000))
                .filter(QueryBuilders.termQuery("brand","华为"))
        );
        //3.发送请求并获取结果
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        //4.解析响应结果
        handleResponse(response);
    }

    //测试排序分页
    @Test
    public void testPageSizeAndSort() throws Exception {
        //1.创建请求对象
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //2.设置请求参数
        searchRequest.source().query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("name","手机"))
                .filter(QueryBuilders.rangeQuery("price").lte(30000))
                .filter(QueryBuilders.termQuery("brand","华为"))
        ).from(0).size(20).sort("price", SortOrder.ASC);
        //3.发送请求并获取结果
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        //4.解析响应结果
        handleResponse(response);
    }

    //高亮显示
    @Test
    public void testHighlight() throws Exception {
        //1.创建请求对象
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //2.设置请求参数
        searchRequest.source()
                .query(QueryBuilders.matchQuery("name","手机"))
                .highlighter(SearchSourceBuilder.highlight().field("name").preTags("<em>").postTags("</em>"));
        //3.发送请求并获取结果
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        //4.解析响应结果
        handleResponse(response);
    }

    //聚合统计
    @Test
    public void testAggregation() throws Exception {
        //1.创建请求对象
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //2.设置请求参数
        searchRequest.source().query(QueryBuilders.termQuery("category","手机"))
                .aggregation(AggregationBuilders.terms("cate_agg").field("brand").size(10).subAggregation(AggregationBuilders.stats("stats_metric").field("price")))
                .size(0);
        //3.发送请求并获取结果
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        //4.解析响应结果
        handleAggregation(response);
    }

    private void handleAggregation(SearchResponse response) {
        Terms terms = response.getAggregations().get("cate_agg");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            System.out.println(bucket.getKey() + ":" + bucket.getDocCount());
            Stats stats = bucket.getAggregations().get("stats_metric");
            System.out.println("max:" + stats.getMax());
        }
    }
}
