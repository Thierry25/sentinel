package com.sentinel.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InstrumentTest {

    @Test
    void shouldThrowOnNullReferencePrice() {
        assertThatThrownBy(() -> new Instrument("AAPL", "sector", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
