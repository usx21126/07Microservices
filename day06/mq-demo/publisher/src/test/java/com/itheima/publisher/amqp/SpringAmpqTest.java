package com.itheima.publisher.amqp;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pm
 * date: 2024/6/11 14:49
 * Description:
 */
@SpringBootTest
public class SpringAmpqTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSimpleQueue(){
        //队列名称
        String queueName = "simple.queue";
        //消息
        String message = "hello,spring amqp!";
        //发送消息
        rabbitTemplate.convertAndSend(queueName,message);
    }


    /*
    测试 work queue；
    向队列发送大量消息，模拟消息堆积
    发送消息，每20毫秒发送一次，相当于每秒发送50条消息
     */
    @Test
    public void testWorkQueue() throws InterruptedException {
        //队列名称
        String queueName = "work.queue";
        //发送的内容
        String message = "hello,workQueue";
        //发送信息
        for (int i = 0; i < 50; i++) {
            rabbitTemplate.convertAndSend(queueName,message+i);
            Thread.sleep(20);
        }
    }


    /*
    测试 fanout exchange；
    向 hmall.fanout 交换机发送消息，消息内容为 hello everyone!，会发送到所有绑定到该交换机的队列
    */
    @Test
    public void testFanoutExchange1(){
        //交换机名字
        String exchangeName = "hmall.fanout";
        //发送内容
        String message = "hello fanout exchange!";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"",message);
    }


    /*
    测试 direct exchange；
    向 hmall.direct 交换机发送消息，会根据路由key发送到所有绑定到该交换机的队列
     */
    @Test
    public void testDirectExchange1(){
        //交换机名字
        String exchangeName = "hmall.direct";
        //发送内容
        String message = "--111--hello direct exchange!";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"blue",message);
        //发送内容
        message = "--222--hello direct exchange!";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"red",message);
        //发送内容
        message = "--333--hello direct exchange!";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"yellow",message);
    }



    /*
    测试 topic exchange；
    向 hmall.topic 交换机发送消息，路由key为china.news 的消息
     */
    @Test
    public void testTopicExchange1(){
        //交换机名字
        String exchangeName = "hmall.topic";
        //发送内容
        String message = "--111--hello topic exchange!";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"china.numberOne",message);
        //发送内容
        message = "--222--hello topic exchange!";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"china.news",message);
        //发送内容
        message = "--333--hello topic exchange!";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"social.news",message);
    }


    /*
    发送map类型消息到 object.queue
     */
    @Test
    public void testMap(){
        String queueName = "object.queue";
        Map<String,Object> map = new HashMap<>();
        map.put("name","黑马");
        map.put("age",18);
        rabbitTemplate.convertAndSend(queueName,map);
    }

    @Test
    public void testPublisherReturnAndConfirm(){
        String exchangeName = "hmall.direct123";
        String message = "hello i am xh ~";

        CorrelationData correlationData = new CorrelationData();
        correlationData.getFuture().addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("ConfirmCallback-----消息发送失败！"+ex.getMessage());
            }

            @Override
            public void onSuccess(CorrelationData.Confirm result) {
                if(result.isAck()){
                    System.out.println("ConfirmCallback-----消息发送成功！");
                }else{
                    System.out.println("ConfirmCallback-----消息发送失败！"+result.getReason());
                }
            }
        });
        rabbitTemplate.convertAndSend(exchangeName,"xxx",message,correlationData);
    }

    @Test
    public void testLazyQueue() throws InterruptedException {
        Message message = MessageBuilder.withBody("hello,lazy queue!".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                .build();
        for (int i = 0; i < 1000000; i++) {
            rabbitTemplate.send("lazy.queue2",message);
            Thread.sleep(1000);
        }
    }

    /*
    发送simple.queue2消息
     */
    @Test
    public void testSimpleQueue2(){
        String queueName = "simple.queue2";
        String message = "hello,simple.queue2!";
        rabbitTemplate.convertAndSend(queueName,message);
    }

    /*
    发送延迟5s的消息到simple.direct
     */
    @Test
    public void testSimpleQueue2Delay() throws InterruptedException {
        String queueName = "simple.direct";
        String message = "hello,simple.queue2!";
        System.out.println("发送时间"+ LocalDateTime.now());
        rabbitTemplate.convertAndSend(queueName,"",message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("5000");
                return message;
            }
        });
    }

    /**
     * 测试发送消息到ttl.fanout
     */
    @Test
    public void testTtlFanout(){
        String exchangeName = "ttl.fanout";
        String message = "hello,ttl.fanout!";
        System.out.println("发送时间"+ LocalTime.now());
        rabbitTemplate.convertAndSend(exchangeName, "blue", message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("5000");
                return message;
            }
        });
    }

    /**
     * 发送延迟5s的消息到delay.direct,路由key为delay
     */
    @Test
    public void testDelayQueue() throws InterruptedException {
        String queueName = "delay.direct";
        String message = "hello,delay.direct!--使用插件版本";
        System.out.println("发送时间"+ LocalTime.now());
        rabbitTemplate.convertAndSend(queueName,"delay",message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(5000);
                return message;
            }
        });
    }
}
