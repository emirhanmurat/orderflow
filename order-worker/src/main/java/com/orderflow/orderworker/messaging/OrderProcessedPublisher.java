package com.orderflow.orderworker.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessedPublisher {
    public static final String EXCHANGE = "orderflow.exchange";
    public static final String ROUTING_KEY = "order.processed";

    private final RabbitTemplate rabbitTemplate;

    public OrderProcessedPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderProcessedEvent event) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}
