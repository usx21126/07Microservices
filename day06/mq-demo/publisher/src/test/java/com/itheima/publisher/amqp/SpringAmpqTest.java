package com.itheima.publisher.amqp;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}
