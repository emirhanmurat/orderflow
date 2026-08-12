package com.orderflow.notification.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderProcessedEvent(
    UUID orderId, String customerId, BigDecimal totalAmount, Instant processedAt
) {}
