package com.sentinel.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderAmendedTest {

    @Test
    void shouldThrowOnNUllEventId() {
        assertThatThrownBy(() -> new OrderAmended(null, "130", "A100", Instant.now(), 15, null));
    }

    @Test
    void shouldThrowOnNUllOrderId() {
        assertThatThrownBy(() -> new OrderAmended("121", null, "A100", Instant.now(), 15, null));
    }

    @Test
    void shouldThrowOnNUllAccountId() {
        assertThatThrownBy(() -> new OrderAmended("121", "1213", null, Instant.now(), 15, null));
    }

    @Test
    void shouldThrowOnNUllTimestamp() {
        assertThatThrownBy(() -> new OrderAmended("121", "1212", "A100", null, 15, null));
    }

    @Test
    void shouldThrowOnNewQtyLessThanZero() {
        assertThatThrownBy(() -> new OrderAmended("121", null, "A100", Instant.now(), -12, null));
    }

    @Test
    void shouldThrowOnNewQtyZero() {
        assertThatThrownBy(() -> new OrderAmended("121", null, "A100", Instant.now(), 0, null));
    }

}
