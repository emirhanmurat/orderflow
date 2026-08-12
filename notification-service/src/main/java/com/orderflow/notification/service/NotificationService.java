package com.orderflow.notification.service;

import com.orderflow.notification.messaging.OrderProcessedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @RabbitListener(queues = "order.processed.queue")
    public void handleOrderProcessed(OrderProcessedEvent event) {
        System.out.println("========================================");
        System.out.println("Sending notification");
        System.out.println("Customer: " + event.customerId());
        System.out.println("Order: " + event.orderId());
        System.out.println("Amount: " + event.totalAmount());
        System.out.println("Message: Your order has been processed.");
        System.out.println("========================================");
    }
}
