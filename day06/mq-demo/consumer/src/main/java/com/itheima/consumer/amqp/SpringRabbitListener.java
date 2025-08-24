package com.itheima.consumer.amqp;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Map;

/**
 * @author pm
 * date: 2024/6/11 14:53
 * Description:
 */
@Slf4j
@Component
public class SpringRabbitListener {

    //singleQueue
    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueue(String msg) {
        System.out.println("SpringRabbitListener ListenSimpleQueueMessage 消费者接收到的消息:"+msg);
    }



    /*
    实现两个消费 work.queue的监听消费消息的方法；
    一个方法消费后沉睡 20毫秒；一个消息消费后沉睡200毫秒；
     */
    @RabbitListener(queues = "work.queue")
    public void listenWorkQueue1(String msg) {
        System.out.println("消费者1接收到消息: "+msg);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(queues = "work.queue")
    public void listenWorkQueue2(String msg) {
        System.out.println("-----消费者2接收到消息: "+msg);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
//
//
//
//
    /*
    监听 fanout.queue1 队列的消息
     */
    @RabbitListener(queues = "fanout.queue1")
    public void listenFanoutQueue1(String msg) {
        System.out.println("消费者1接收到消息: "+msg);
    }
    /*
    监听 fanout.queue2 队列的消息
     */
    @RabbitListener(queues = "fanout.queue2")
    public void listenFanoutQueue2(String msg) {
        System.out.println("消费者2接收到消息: "+msg);
    }



    /*
    监听 direct.queue1 队列的消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "direct.queue1",durable = "true"),
            exchange = @Exchange(name = "hmall.direct",type = ExchangeTypes.DIRECT),
            key = {"blue","red"}
    ))
    public void listenDirectQueue1(String msg) {
        System.out.println("消费者1接收到消息: "+msg);
    }

    /*
    监听 direct.queue2 队列的消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "direct.queue2",durable = "true"),
            exchange = @Exchange(name = "hmall.direct",type = ExchangeTypes.DIRECT),
            key = {"red","yellow"}
    ))
    public void listenDirectQueue2(String message){
        System.out.println("消费者2接收到消息: "+message);
    }



    /*
    监听 topic.queue1 队列的消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "topic.queue1",durable = "true"),
            exchange = @Exchange(name = "hmall.topic",type = ExchangeTypes.TOPIC),
            key = "china.#"
    ))
    public void listenTopicQueue1(String message){
        System.out.println("消费者1接收到消息: "+message);
    }
    /*
    监听 topic.queue2 队列的消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "topic.queue2",durable = "true"),
            exchange = @Exchange(name = "hmall.topic",type = ExchangeTypes.TOPIC),
            key = "#.news"
    ))
    public void listenTopicQueue2(String message){
        System.out.println("消费者2接收到消息: "+message);
    }


     /*
    监听 object.queue 队列的消息
     */
    @RabbitListener(queues = "object.queue")
    public void listenObjectQueue(Map<String,Object> map){
        System.out.println("消费者接收到消息: "+map);
    }
}
