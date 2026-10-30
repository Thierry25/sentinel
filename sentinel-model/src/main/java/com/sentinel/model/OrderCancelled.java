package com.sentinel.model;

import java.time.Instant;

public record OrderCancelled(String eventId,
                             String orderId,
                             String accountId,
                             Instant timestamp) implements TradeEvent {
    public OrderCancelled {
        if (eventId == null) throw new IllegalArgumentException("eventId cannot be null");
        if (orderId == null) throw new IllegalArgumentException("orderId cannot be null");
        if (accountId == null) throw new IllegalArgumentException("accountId cannot be null");
        if (timestamp == null) throw new IllegalArgumentException("timestamp cannot be null");
    }
}
