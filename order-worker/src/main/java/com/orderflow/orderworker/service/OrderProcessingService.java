package com.orderflow.orderworker.service;

import com.orderflow.orderworker.entity.Order;
import com.orderflow.orderworker.entity.OrderStatus;
import com.orderflow.orderworker.messaging.*;
import com.orderflow.orderworker.repository.OrderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class OrderProcessingService {
    private final OrderRepository orderRepository;
    private final OrderProcessedPublisher eventPublisher;

    public OrderProcessingService(OrderRepository orderRepository,
                                  OrderProcessedPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @RabbitListener(queues = "order.created.queue")
    @Transactional
    public void processOrder(OrderCreatedEvent event) {
        System.out.println("Received order: " + event.orderId());

        Order order = orderRepository.findById(event.orderId())
            .orElseThrow(() -> new IllegalStateException(
                "Order does not exist: " + event.orderId()));

        if (order.getStatus() == OrderStatus.PROCESSED) {
            System.out.println("Order already processed: " + event.orderId());
            return;
        }

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        simulateProcessing();

        order.setStatus(OrderStatus.PROCESSED);
        orderRepository.save(order);

        eventPublisher.publish(new OrderProcessedEvent(
            order.getId(), order.getCustomerId(),
            order.getTotalAmount(), Instant.now()
        ));

        System.out.println("Order processed: " + order.getId());
    }

    private void simulateProcessing() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Order processing interrupted", e);
        }
    }
}
