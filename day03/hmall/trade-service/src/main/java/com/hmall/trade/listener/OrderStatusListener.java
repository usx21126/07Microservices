package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.common.domain.MultiDelayMessage;
import com.hmall.trade.constants.TradeMqConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusListener {

    private final IOrderService orderService;
    private final PayClient payClient;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 监听延迟消息中的订单状态
     * 1、获取订单id
     * 2、查询订单；判断订单状态是否为 未支付；其它则不更新
     * 3、未支付订单，则查询订单信息
     * 3.1、如果支付成功，更新订单状态
     * 3.2、如果还有剩余延迟时间，则继续发送下一个延迟消息；
     * 4、如果超时则取消订单
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = TradeMqConstants.DELAY_ORDER_QUEUE, durable = "true"),
            exchange = @Exchange(value = TradeMqConstants.DELAY_EXCHANGE, delayed = "true", type = ExchangeTypes.TOPIC, durable = "true"),
            key = TradeMqConstants.DELAY_ORDER_ROUTING_KEY
    ))
    public void listenOrderCheckDelayMessage(MultiDelayMessage<Long> message) {
        // 1、获取订单id
        Long orderId = message.getData();
        // 2、查询订单；判断订单状态是否为 未支付；其它则不更新
        Order order = orderService.getById(orderId);
        if (order == null || order.getStatus() > 1) {
            //订单不存在或订单状态已经支付，放弃处理
            return;
        }
        // 3、未支付订单，则查询订单信息
        PayOrderDTO payOrderDTO = payClient.queryPayOrderDTOByBizOrderNo(orderId);

        // 3.1、如果支付成功，更新订单状态
        if (payOrderDTO != null && payOrderDTO.getStatus() == 3) {
            orderService.markOrderPaySuccess(orderId);
            return;
        }
        // 3.2、如果还有剩余延迟时间，则继续发送下一个延迟消息；
        if (message.hasNextDelay()) {
            //① 获取下一个延迟时间
            Integer delayTime = message.removeNextDelay();
            //② 再次发送延迟消息
            rabbitTemplate.convertAndSend(TradeMqConstants.DELAY_EXCHANGE, TradeMqConstants.DELAY_ORDER_ROUTING_KEY, message, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setDelay(delayTime);
                    return message;
                }
            });
            return;
        }
        // 4、如果超时则取消订单；cancelOrder是没有的方法，占个位；后实现
        orderService.cancelOrder(orderId);
    }
}
