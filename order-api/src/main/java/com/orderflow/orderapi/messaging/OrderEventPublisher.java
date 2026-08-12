package com.orderflow.orderapi.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {
    public static final String EXCHANGE = "orderflow.exchange";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(EXCHANGE, ORDER_CREATED_ROUTING_KEY, event);
    }
}
