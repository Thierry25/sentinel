package com.sentinel.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderFilledTest {

    @Test
    void shouldThrowOnNUllEventId() {
        assertThatThrownBy(() -> new OrderFilled(null, "orderid", "A100", Instant.now(),
                "MSF", Side.SELL, 200, Money.of("100", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllOrderId() {
        assertThatThrownBy(() -> new OrderFilled("91", null, "A100", Instant.now(),
                "MSF", Side.SELL, 200,  Money.of("100", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllAccountId() {
        assertThatThrownBy(() -> new OrderFilled("192", "orderid", null, Instant.now(),
                "MSF", Side.SELL, 200,  Money.of("100", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllTimestamp() {
        assertThatThrownBy(() -> new OrderFilled("1029", "orderid", "A100", null,
                "MSF", Side.SELL, 200,  Money.of("100", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllSymbol() {
        assertThatThrownBy(() -> new OrderFilled("1923", "orderid", "A100", Instant.now(),
                null, Side.SELL, 200,  Money.of("100", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllSide() {
        assertThatThrownBy(() -> new OrderFilled("1219", "orderid", "A100", Instant.now(),
                "MSF", null, 200,  Money.of("100", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllPrice() {
        assertThatThrownBy(() -> new OrderFilled("1219", "orderid", "A100", Instant.now(),
                "MSF", Side.BUY, 200,  null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNegativeQuantity() {
        assertThatThrownBy(() -> new OrderFilled("1212", "orderid", "A100", Instant.now(),
                "MSF", Side.SELL, -122,  Money.of("100", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnZeroQuantity() {
        assertThatThrownBy(() -> new OrderFilled("1212", "orderid", "A100", Instant.now(),
                "MSF", Side.SELL, 0,  Money.of("100", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
