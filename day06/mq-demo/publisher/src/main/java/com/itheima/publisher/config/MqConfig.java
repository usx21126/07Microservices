    package com.itheima.publisher.config;

    import lombok.RequiredArgsConstructor;
    import org.springframework.amqp.core.ReturnedMessage;
    import org.springframework.amqp.rabbit.core.RabbitTemplate;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.context.annotation.Configuration;

    import javax.annotation.PostConstruct;

    @Configuration
    @RequiredArgsConstructor
    public class MqConfig {
        private final RabbitTemplate rabbitTemplate;

        //对rabbittemplate设置return异常处理
        @PostConstruct
        public void initRabbitTemplate() {
            rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
                @Override
                public void returnedMessage(ReturnedMessage returnedMessage) {
                    System.out.println("--------------ReturnCallback-----------------");
                    System.out.println("交换机： "+returnedMessage.getExchange());
                    System.out.println("路由键： "+returnedMessage.getRoutingKey());
                    System.out.println("消息： "+returnedMessage.getMessage());
                    System.out.println("ReplyCode： "+returnedMessage.getReplyCode());
                    System.out.println("ReplyText： "+returnedMessage.getReplyText());
                }
            });
        }
    }
