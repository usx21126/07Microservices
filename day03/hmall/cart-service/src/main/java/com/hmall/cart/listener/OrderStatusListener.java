package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.constants.MqConstants;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStatusListener {

    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(value = MqConstants.TRADE_EXCHANGE_NAME, type = ExchangeTypes.TOPIC, durable = "true"),
            key = {MqConstants.ROUTING_KEY_ORDER_CREATE}
    ))
    public void listenOrderCreate(List<Long> itemIds, @Header("user-info")Long userId) {
        //获取当前用户id
        UserContext.setUser(userId);
        cartService.removeByItemIds(itemIds);

        //删除用户id
        UserContext.removeUser();
    }
}
