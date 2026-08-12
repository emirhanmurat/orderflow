package com.orderflow.orderapi.service;

import com.orderflow.orderapi.dto.*;
import com.orderflow.orderapi.entity.*;
import com.orderflow.orderapi.exception.OrderNotFoundException;
import com.orderflow.orderapi.messaging.*;
import com.orderflow.orderapi.repository.OrderRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Service
public class OrderService {
    private static final String CACHE_PREFIX = "order:";
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    public OrderService(OrderRepository orderRepository, OrderEventPublisher eventPublisher,
                        RedisTemplate<String, Object> redisTemplate) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.customerId());
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.productId());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice());
            order.addItem(item);
            total = total.add(itemRequest.unitPrice()
                .multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        eventPublisher.publishOrderCreated(new OrderCreatedEvent(
            saved.getId(), saved.getCustomerId(), saved.getTotalAmount(), saved.getCreatedAt()
        ));

        OrderResponse response = toResponse(saved);
        cacheOrder(response);
        return response;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        Object cached = redisTemplate.opsForValue().get(CACHE_PREFIX + orderId);
        if (cached instanceof OrderResponse response) return response;

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderResponse response = toResponse(order);
        cacheOrder(response);
        return response;
    }

    private void cacheOrder(OrderResponse response) {
        redisTemplate.opsForValue().set(
            CACHE_PREFIX + response.id(), response, Duration.ofMinutes(10)
        );
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
            order.getId(), order.getCustomerId(), order.getStatus(),
            order.getTotalAmount(), order.getCreatedAt(), order.getUpdatedAt(),
            order.getItems().stream().map(item -> new OrderItemResponse(
                item.getId(), item.getProductId(), item.getQuantity(), item.getUnitPrice()
            )).toList()
        );
    }
}
