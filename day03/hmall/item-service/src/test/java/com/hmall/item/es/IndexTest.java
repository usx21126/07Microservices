package com.hmall.item.es;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
@SpringBootTest
public class IndexTest {
    //es操作的客户端对象
    private RestHighLevelClient client;
    //索引库名称
    private static final String INDEX_NAME = "items";
    @Autowired
    private IItemService itemService;
    //索引库映射名称
    private static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\"\n" +
            "      },\n" +
            "      \"price\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"stock\":{\n" +
            "        \"type\":\"integer\"\n" +
            "      },\n" +
            "      \"image\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"sold\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"isAD\":{\n" +
            "        \"type\": \"boolean\"\n" +
            "      },\n" +
            "      \"updateTime\":{\n" +
            "        \"type\": \"date\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    @BeforeEach
    public void init() {
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.12.168:9200")));
    }

    @Test
    public void testClient(){
        System.out.println(client);
    }
    /*
    创建索引库
     */
    @Test
    public void createIndex() throws IOException {
        //1.创建请求对象
        CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
        //2.设置请求参数
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        //3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    //测试查询索引库items是否存在
    @Test
    public void testExistsIndex() throws Exception {
        //1、创建查询索引库请求对象
        GetIndexRequest request = new GetIndexRequest("items");
        //2、发送请求
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists?"索引库已经存在":"索引库不存在");
    }

    //测试删除索引库 items
    @Test
    public void testDeleteIndex() throws Exception {
        //1、创建删除索引库请求对象
        DeleteIndexRequest request = new DeleteIndexRequest("items");
        //2、发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    //批量导入商品到es
    @Test
    public void testImportItem() throws IOException {
        //页号
        int pageNo = 1;
        // 页大小
        int pageSize = 1000;
        while (true) {
            System.out.println("--------------正在导入第" + pageNo + "页数据...----------------");
            // 1. 根据页号，页大小每次查询1000条数据
            Page<Item> page = itemService.lambdaQuery().eq(Item::getStatus, 1).page(new Page<>(pageNo, pageSize));
            List<Item> itemList = page.getRecords();
            if(CollUtils.isEmpty(itemList)){
                break;
            }
            // 2. 将每条Item转为ItemDoc 并设置到IndexRequest
            List<ItemDoc> itemDocList = BeanUtils.copyList(itemList, ItemDoc.class);
            // 3. 批量插入
            BulkRequest bulkRequest = new BulkRequest();
            for (ItemDoc itemDoc : itemDocList) {
                bulkRequest.add(new IndexRequest(INDEX_NAME).id(itemDoc.getId().toString()).source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON));
            }
            // 4.提交BulkRequest
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println("-----------第" + pageNo + "页数据导入完成...----------------");
            // 5. 继续分页查询，直到没有数据为止
            pageNo++;
        }
    }

    @AfterEach
    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
