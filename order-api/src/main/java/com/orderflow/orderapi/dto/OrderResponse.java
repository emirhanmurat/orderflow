package com.orderflow.orderapi.dto;

import com.orderflow.orderapi.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id, String customerId, OrderStatus status, BigDecimal totalAmount,
    Instant createdAt, Instant updatedAt, List<OrderItemResponse> items
) {}
