package com.sentinel.simulator;

import com.sentinel.model.Money;
import com.sentinel.model.OrderCancelled;
import com.sentinel.model.OrderFilled;
import com.sentinel.model.OrderPlaced;
import com.sentinel.model.Side;
import com.sentinel.model.TradeEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** TestEvents.filled().order("O1").account("A1").symbol("AAPL").quantity(100).price("230.00").build() */
public final class TestEvents {

    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

    private TestEvents() {
    }

    public static Builder placed()    { return new Builder(Kind.PLACED); }
    public static Builder filled()    { return new Builder(Kind.FILLED); }
    public static Builder cancelled() { return new Builder(Kind.CANCELLED); }

    private enum Kind { PLACED, FILLED, CANCELLED }

    public static final class Builder {
        private final Kind kind;
        private String orderId = "O1";
        private String accountId = "A1";
        private String symbol = "AAPL";
        private Side side = Side.BUY;
        private long quantity = 100;
        private String price = "100.00";
        private LocalTime time = LocalTime.of(10, 0);

        private Builder(Kind kind) { this.kind = kind; }

        public Builder order(String orderId)     { this.orderId = orderId; return this; }
        public Builder account(String accountId) { this.accountId = accountId; return this; }
        public Builder symbol(String symbol)     { this.symbol = symbol; return this; }
        public Builder side(Side side)           { this.side = side; return this; }
        public Builder quantity(long quantity)   { this.quantity = quantity; return this; }
        public Builder price(String price)       { this.price = price; return this; }
        public Builder at(String time)           { this.time = LocalTime.parse(time); return this; }

        public TradeEvent build() {
            var timestamp = ZonedDateTime.of(LocalDate.of(2026, 10, 27), time, NEW_YORK).toInstant();
            var money = Money.of(price, "USD");
            return switch (kind) {
                case PLACED -> new OrderPlaced("P-" + orderId, orderId, accountId, timestamp, symbol, side, quantity, money);
                case FILLED -> new OrderFilled("F-" + orderId, orderId, accountId, timestamp, symbol, side, quantity, money);
                case CANCELLED -> new OrderCancelled("C-" + orderId, orderId, accountId, timestamp);
            };
        }
    }
}