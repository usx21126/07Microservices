package com.hmall.api.fallback;

import com.hmall.api.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
@Slf4j
public class UserClientFallback implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public void deductMoney(String pw, Integer amount) {
                log.error("用户服务调用失败 -> 具体参数为{},{}", pw, amount,cause);
                throw new RuntimeException("用户服务调用失败");
            }
        };
    }
}
