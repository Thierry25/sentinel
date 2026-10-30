package com.sentinel.simulator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IdGeneratorTest {

    @Test
    void nextEventId() {
        var eventId = IdGenerator.nextEventId();
        assertThat(eventId).isEqualTo("E-0000001");

        eventId = IdGenerator.nextEventId();
        assertThat(eventId).isEqualTo("E-0000002");
    }

    @Test
    void nextOrderId() {
        var orderId = IdGenerator.nextOrderId();
        assertThat(orderId).isEqualTo("O-0000001");

        orderId = IdGenerator.nextOrderId();
        assertThat(orderId).isEqualTo("O-0000002");
    }
}
