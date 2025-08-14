package com.hmall.api.fallback;

import com.hmall.api.client.TradeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
@Slf4j
public class TradeClientFallback implements FallbackFactory<TradeClient> {
    @Override
    public TradeClient create(Throwable cause) {
        return new TradeClient() {
            @Override
            public void markOrderPaySuccess(Long orderId) {
                log.error("远程调用TradeClient.markOrderPaySuccess 失败；orderId = {}", orderId, cause);
                throw new RuntimeException("订单服务调用失败");
            }
        };
    }
}
