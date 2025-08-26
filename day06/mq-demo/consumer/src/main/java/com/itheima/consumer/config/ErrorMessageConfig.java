package com.itheima.consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name="spring.rabbitmq.listener.simple.retry.enabled", havingValue = "true")
public class ErrorMessageConfig {
    //定义名字为的error.direct的direct交换机
    @Bean
    public DirectExchange errorDirectExchange() {
        return new DirectExchange("error.direct");
    }
    //定义名字为error.queue的队列
    @Bean
    public Queue errorQueue() {
        return new Queue("error.queue", true);
    }
    //error.direct交换机和error.queue队列绑定
    @Bean
    public Binding errorBinding(Queue errorQueue, DirectExchange errorDirectExchange) {
        return BindingBuilder.bind(errorQueue).to(errorDirectExchange).with("error");
    }

    //设置RabbitTemplate在尝试接收最大次数之后投递的交换机和路由key
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, "error.direct", "error");
    }
}
