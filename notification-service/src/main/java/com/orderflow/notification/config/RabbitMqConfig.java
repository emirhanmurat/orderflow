package com.orderflow.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EXCHANGE = "orderflow.exchange";
    public static final String QUEUE = "order.processed.queue";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue orderProcessedQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding orderProcessedBinding(Queue orderProcessedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderProcessedQueue).to(orderExchange).with("order.processed");
    }
}
