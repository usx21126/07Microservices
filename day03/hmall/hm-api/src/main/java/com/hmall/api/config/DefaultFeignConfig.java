package com.hmall.api.config;

import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {

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
