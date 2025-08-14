package com.hmall.api.fallback;

import com.hmall.api.client.CartClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;

@Slf4j
public class CartClientFallback implements FallbackFactory<CartClient> {
    @Override
    public CartClient create(Throwable cause) {
        return new CartClient() {
            @Override
            public void deleteCartItemByIds(Collection<Long> ids) {
                log.error("购物车服务调用失败 -> 具体参数为：{}",ids,cause);
                throw new RuntimeException("购物车服务调用失败");
            }
        };
    }
}
