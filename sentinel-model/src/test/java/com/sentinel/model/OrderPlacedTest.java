package com.sentinel.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderPlacedTest {

    @Test
    void shouldThrowOnNUllEventId() {
        assertThatThrownBy(() -> new OrderPlaced(null, "orderid", "A100", Instant.now(),
                "MSF", Side.SELL, 200, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllOrderId() {
        assertThatThrownBy(() -> new OrderPlaced("91", null, "A100", Instant.now(),
                "MSF", Side.SELL, 200, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllAccountId() {
        assertThatThrownBy(() -> new OrderPlaced("192", "orderid", null, Instant.now(),
                "MSF", Side.SELL, 200, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllTimestamp() {
        assertThatThrownBy(() -> new OrderPlaced("1029", "orderid", "A100", null,
                "MSF", Side.SELL, 200, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllSymbol() {
        assertThatThrownBy(() -> new OrderPlaced("1923", "orderid", "A100", Instant.now(),
                null, Side.SELL, 200, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllSide() {
        assertThatThrownBy(() -> new OrderPlaced("1219", "orderid", "A100", Instant.now(),
                "MSF", null, 200, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNegativeQuantity() {
        assertThatThrownBy(() -> new OrderPlaced("1212", "orderid", "A100", Instant.now(),
                "MSF", Side.SELL, -122, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnZeroQuantity() {
        assertThatThrownBy(() -> new OrderPlaced("1212", "orderid", "A100", Instant.now(),
                "MSF", Side.SELL, 0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
