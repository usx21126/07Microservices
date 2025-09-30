package com.hmall.search.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.domain.vo.PageVO;
import com.hmall.search.service.ISearchService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements ISearchService {
    //索引库名称
    private static final String INDEX_NAME = "items";

    @Autowired
    private ItemClient itemClient;

    private RestHighLevelClient client;

    public SearchServiceImpl(){
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.12.168:9200")));
    }

    @Override
    public void saveItemById(Long itemId) {
        try {
            //1、根据商品id查询商品信息
            ItemDTO itemDTO = itemClient.queryItemById(itemId);
            if (itemDTO != null) {
                //2、转换为itemDoc json字符串
                ItemDoc itemDoc = BeanUtils.copyBean(itemDTO, ItemDoc.class);
                String jsonStr = JSONUtil.toJsonStr(itemDoc);
                //3、创建新增请求
                IndexRequest request = new IndexRequest(INDEX_NAME).id(itemDoc.getId().toString());
                //4、设置source参数
                request.source(jsonStr, XContentType.JSON);
                //5、发送请求
                client.index(request, RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            throw new RuntimeException("更新es中商品失败！参数:" + itemId, e);
        }
    }

    @Override
    public void deleteItemById(Long itemId) {
        try {
            //1、创建删除请求
            DeleteRequest deleteRequest = new DeleteRequest(INDEX_NAME, itemId.toString());
            //2、发送请求
            client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("删除es中商品失败！参数:" + itemId, e);
        }
    }

    @Override
    public PageVO<ItemDoc> search(ItemPageQuery query) {
        PageVO<ItemDoc> pageVO = PageVO.empty(0L, 0L);
        try {
            //1、创建查询请求
            SearchRequest request = new SearchRequest(INDEX_NAME);
            //2、设置查询及各类参数
            //创建bool查询
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            //设置搜索关键字
            boolean isHighlight = false;
            if (StrUtil.isNotBlank(query.getKey())) {
                boolQuery.must(QueryBuilders.matchQuery("name", query.getKey()));

                //只有搜索了关键字才高亮
                isHighlight = true;
            }
            //设置分类过滤查询
            if (StrUtil.isNotBlank(query.getCategory())) {
                boolQuery.filter(QueryBuilders.termQuery("category", query.getCategory()));
            }
            //设置品牌过滤查询
            if (StrUtil.isNotBlank(query.getBrand())) {
                boolQuery.filter(QueryBuilders.termQuery("brand", query.getBrand()));
            }
            //设置价格过滤查询
            if (query.getMinPrice() != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
            }
            if (query.getMaxPrice() != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
            }
            if (isHighlight) {
                //设置高亮
                request.source().highlighter(SearchSourceBuilder.highlight()
                        .field("name")
                        .preTags("<em>")
                        .postTags("</em>")
                );
            }
            //设置分页
            int pageNo = query.getPageNo();
            int pageSize = query.getPageSize();
            request.source().from((pageNo - 1) * pageSize).size(pageSize);
            //设置排序
            if (StrUtil.isNotBlank(query.getSortBy())) {
                request.source().sort(query.getSortBy(), query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC);
            }
            FunctionScoreQueryBuilder queryBuilder = QueryBuilders.functionScoreQuery(
                    boolQuery,
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    QueryBuilders.termQuery("isAD", true),
                                    ScoreFunctionBuilders.weightFactorFunction(10)
                            )
                    }).boostMode(CombineFunction.MULTIPLY);

            //设置查询对象
            request.source().query(queryBuilder);
            //3、发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4、解析响应结果
            SearchHits hits = response.getHits();
            //总记录数
            long total = hits.getTotalHits().value;
            pageVO.setTotal(total);
            //通过页大小和总记录数计算总页数
            long pages = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
            pageVO.setPages(pages);

            List<ItemDoc> itemDocList = new ArrayList<>(pageSize);
            for (SearchHit hit : hits.getHits()) {
                ItemDoc itemDoc = JSONUtil.toBean(hit.getSourceAsString(), ItemDoc.class);
                //处理高亮
                if (isHighlight) {
                    HighlightField highlightField = hit.getHighlightFields().get("name");
                    if (highlightField != null) {
                        String name = highlightField.getFragments()[0].string();
                        itemDoc.setName(name);
                    }
                }
                itemDocList.add(itemDoc);
            }
            pageVO.setList(itemDocList);

            return pageVO;
        } catch (IOException e) {
            throw new RuntimeException("查询es中商品失败！", e);
        }
    }

    @Override
    public Map<String, List<String>> filters(ItemPageQuery query) {
        try {
            //只有当分类或品牌没有选择的时才有必要去查对应的数据
            if (StrUtil.isBlank(query.getCategory()) || StrUtil.isBlank(query.getBrand())) {
                Map<String, List<String>> resultMap = new HashMap<>();
                //1、创建查询请求
                SearchRequest request = new SearchRequest(INDEX_NAME);
                //2、设置参数
                //是否需要查询分类聚合数据
                boolean isNeedCategoryAgg = true;
                //是否需要查询品牌聚合数据
                boolean isNeedBrandAgg = true;
                //设置搜索关键字
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                if (StrUtil.isNotBlank(query.getKey())) {
                    boolQuery.must(QueryBuilders.matchQuery("name", query.getKey()));
                }
                if (StrUtil.isNotBlank(query.getCategory())) {
                    boolQuery.filter(QueryBuilders.termQuery("category", query.getCategory()));
                    isNeedCategoryAgg = false;
                }
                if (StrUtil.isNotBlank(query.getBrand())) {
                    boolQuery.filter(QueryBuilders.termQuery("brand", query.getBrand()));
                    isNeedBrandAgg = false;
                }
                if (query.getMinPrice() != null) {
                    boolQuery.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
                }
                if (query.getMaxPrice() != null) {
                    boolQuery.filter(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
                }
                request.source().query(boolQuery);

                //设置不返回文档
                request.source().size(0);

                //设置分类聚合
                if (isNeedCategoryAgg) {
                    TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("category_agg").field("category").size(20);
                    request.source().aggregation(aggregationBuilder);
                }
                //设置品牌聚合
                if (isNeedBrandAgg) {
                    TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("brand_agg").field("brand").size(20);
                    request.source().aggregation(aggregationBuilder);
                }

                //3、发送请求
                SearchResponse response = client.search(request, RequestOptions.DEFAULT);
                //4、解析响应结果
                Aggregations aggregations = response.getAggregations();
                Terms categoryAgg = aggregations.get("category_agg");
                if (categoryAgg != null) {
                    List<String> categoryList = new ArrayList<>();
                    for (Terms.Bucket bucket : categoryAgg.getBuckets()) {
                        categoryList.add(bucket.getKeyAsString());
                    }
                    resultMap.put("category", categoryList);
                }
                Terms brandAgg = aggregations.get("brand_agg");
                if (brandAgg != null) {
                    List<String> brandList = new ArrayList<>();
                    for (Terms.Bucket bucket : brandAgg.getBuckets()) {
                        brandList.add(bucket.getKeyAsString());
                    }
                    resultMap.put("brand", brandList);
                }
                return resultMap;
            }
        } catch (IOException e) {
            System.out.println("查询分类、品牌聚合数据失败！" + e);
        }
        return CollUtils.emptyMap();
    }
}
