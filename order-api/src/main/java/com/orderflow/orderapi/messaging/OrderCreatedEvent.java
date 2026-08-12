package com.orderflow.orderapi.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId, String customerId, BigDecimal totalAmount, Instant createdAt
) {}
