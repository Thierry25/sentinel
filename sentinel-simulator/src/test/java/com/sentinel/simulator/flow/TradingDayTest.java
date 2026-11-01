package com.sentinel.simulator.flow;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class TradingDayTest {

    @Test
    void testOpen() {
        var tradingDay = new TradingDay(LocalDate.of(2026, 10, 27));
        assertThat(tradingDay.open()).isEqualTo(Instant.parse("2026-10-27T13:30:00Z"));
    }

    @Test
    void testClose() {
        var tradingDay = new TradingDay(LocalDate.of(2026, 10, 27));
        assertThat(tradingDay.close()).isEqualTo(Instant.parse("2026-10-27T20:00:00Z"));
    }
}
