package com.hmall.api.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;
@Slf4j
public class ItemClientFallback implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient(){
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("商品服务调用失败 -> 具体参数为：{}",ids ,cause );
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("商品服务调用失败 -> 具体参数为：{}",items );
                throw new RuntimeException("商品服务调用失败");
            }

            @Override
            public ItemDTO queryItemById(Long id) {
                log.error("远程调用ItemClient.queryItemById失败,参数：{}",id,cause);
                return null;
            }
        };
    }
}
