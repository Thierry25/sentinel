package com.sentinel.model;

import java.time.Instant;

public sealed interface TradeEvent permits OrderPlaced, OrderAmended, OrderCancelled, OrderFilled {
    String eventId();
    String orderId();
    String accountId();
    Instant timestamp();
}
