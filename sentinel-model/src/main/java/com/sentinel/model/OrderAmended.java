package com.sentinel.model;

import java.time.Instant;

public record OrderAmended(String eventId,
                           String orderId,
                           String accountId,
                           Instant timestamp,
                           long newQuantity,
                           Money newLimitPrice) implements TradeEvent {

    public OrderAmended {
        if (eventId == null) throw new IllegalArgumentException("eventId cannot be null");
        if (orderId == null) throw new IllegalArgumentException("orderId cannot be null");
        if (accountId == null) throw new IllegalArgumentException("accountId cannot be null");
        if (timestamp == null) throw new IllegalArgumentException("timestamp cannot be null");

        if (newQuantity == 0) throw new IllegalArgumentException("Quantity cannot be zero");
        if (newQuantity < 0) throw  new IllegalArgumentException("Quantity cannot be less than zero");
    }
}
