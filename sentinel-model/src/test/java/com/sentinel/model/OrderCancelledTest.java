package com.sentinel.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderCancelledTest {

    @Test
    void shouldThrowOnNUllEventId() {
        assertThatThrownBy(() -> new OrderCancelled(null, "139", "A100", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllOrderId() {
        assertThatThrownBy(() -> new OrderCancelled("3232", null, "A100", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUlAccountId() {
        assertThatThrownBy(() -> new OrderCancelled("2302", "139", null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNUllTimestamp() {
        assertThatThrownBy(() -> new OrderCancelled("130", "139", "A100", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}


