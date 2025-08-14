package com.hmall.api.config;

import com.hmall.api.client.ItemClient;
import com.hmall.api.client.TradeClient;
import com.hmall.api.client.UserClient;
import com.hmall.api.fallback.CartClientFallback;
import com.hmall.api.fallback.ItemClientFallback;
import com.hmall.api.fallback.TradeClientFallback;
import com.hmall.api.fallback.UserClientFallback;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    //注册关于商品远程调用客户端的fallback
    @Bean
    public ItemClientFallback itemClientFallback(){
        return new ItemClientFallback();
    }
    @Bean
    public CartClientFallback cartClientFallback(){
        return new CartClientFallback();
    }
    @Bean
    public TradeClientFallback tradeClient(){
        return new TradeClientFallback();
    }
    @Bean
    public UserClientFallback userClient(){
        return new UserClientFallback();
    }
    //定义feign请求拦截器，设置用户信息
    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                Long userId = UserContext.getUser();
                if(userId!= null){
                    template.header("user-info", userId.toString());
                    System.out.println("DefaultFeignConfig执行了 -- userId:"+userId);
                }
            }
        };
    }

    //配置feign的日志级别
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }
}
