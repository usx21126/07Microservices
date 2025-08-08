package com.hmall.api.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


public class DefaultFeignConfig {
    // 日志记录级别：none basic headers full
    @Bean
    public Logger.Level defaultFeignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
