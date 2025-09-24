package com.hmall.item.es;

import cn.hutool.json.JSONUtil;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(properties = "spring.profiles.active=local")
public class DocumentTest {
    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;

    //初始化client
    @BeforeEach
    public void initClient() {
        client = new RestHighLevelClient(
                RestClient.builder(HttpHost.create("http://192.168.12.168:9200")));
    }

    /**
     * 新增商品文档
     * @throws IOException
     */
    @Test
    public void testCreateIndex() throws IOException {
        //1.根据商品id查询mysql中的商品
        Item item = itemService.getById(577967L);
        //2.将Item对象转换为es可以接受的对象ItemDoc
        ItemDoc itemDoc = BeanUtils.copyBean(item, ItemDoc.class);
        //3.创建请求对象
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId().toString());
        //4.设置请求参数
        String jsonStr = JSONUtil.toJsonStr(itemDoc);
        request.source(jsonStr, XContentType.JSON);
        //5.发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 查询商品文档
     * @throws IOException
     */
    @Test
    public void testGetDocumentById() throws IOException {
        //1.创建请求对象
        GetRequest request = new GetRequest("items", "577967");
        //2.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        //3.处理响应结果
        ItemDoc itemDoc = JSONUtil.toBean(response.getSourceAsString(), ItemDoc.class);
        System.out.println(itemDoc);
    }

    /**
     * 更新商品文档
     * @throws IOException
     */
    @Test
    public void testUpdateDocumentById() throws IOException {
        UpdateRequest request = new UpdateRequest("items", "577967");
        request.doc("price", 71301,"commentCount",10);
        client.update(request, RequestOptions.DEFAULT);
    }
    @Test
    public void testDeleteDocumentById() throws IOException {
        DeleteRequest request = new DeleteRequest("items", "577967");
        client.delete(request, RequestOptions.DEFAULT);
    }
    //关闭client
    @AfterEach
    public void closeClient() throws IOException {
        client.close();
    }
}
