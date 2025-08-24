package com.itheima.consumer.config;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pm
 * date: 2024/6/11 19:06
 * Description:
 */
@Configuration
public class MessageConfig {

    @Bean
    public Queue objectQueue() {
        return new Queue("object.queue");
    }
//    @Bean
//    public MessageConverter messageConverter() {
//        //1、定义消息转换器
//        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
//        //2、配置每条消息自动创建id；用于识别不同消息，也可以在页面中基于id判断是否是重复消息
//        jackson2JsonMessageConverter.setCreateMessageIds(true);
//        return jackson2JsonMessageConverter;
//    }

}
