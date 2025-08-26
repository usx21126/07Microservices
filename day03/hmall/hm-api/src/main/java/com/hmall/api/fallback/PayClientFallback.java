package com.hmall.api.fallback;


import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class PayClientFallback implements FallbackFactory<PayClient> {
    public PayClient create(Throwable cause) {
        return new PayClient() {
            @Override
            public PayOrderDTO queryPayOrderDTOByBizOrderNo(Long id) {
                log.error("远程调用PayClient.queryPayOrderByBizOrderNo 失败；参数{}", id, cause);
                return null;
            }
        };
    }
}
