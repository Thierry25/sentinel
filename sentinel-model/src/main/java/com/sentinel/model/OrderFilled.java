package com.sentinel.model;

import java.time.Instant;

public record OrderFilled(String eventId,
                          String orderId,
                          String accountId,
                          Instant timestamp,
                          String symbol,
                          Side side,
                          long quantity,
                          Money price) implements TradeEvent {

    public OrderFilled {
        if (eventId == null) throw new IllegalArgumentException("eventId cannot be null");
        if (orderId == null) throw new IllegalArgumentException("orderId cannot be null");
        if (accountId == null) throw new IllegalArgumentException("accountId cannot be null");
        if (timestamp == null) throw new IllegalArgumentException("timestamp cannot be null");
        if (symbol == null) throw new IllegalArgumentException("symbol cannot be null");
        if (side == null) throw new IllegalArgumentException("side cannot be null");

        if (quantity == 0) throw new IllegalArgumentException("Quantity cannot be zero");
        if (quantity < 0) throw  new IllegalArgumentException("Quantity cannot be less than zero");

        if (price == null) throw  new IllegalArgumentException("price cannot be null");
    }
}
