package com.sentinel.simulator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SimulationConfigTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 27);

    // 1. defaults(seed) fills in the standard values; only the seed comes from you
    @Test
    void defaultsUseTheStandardValues() {
        assertThat(SimulationConfig.defaults(42))
                .isEqualTo(new SimulationConfig(42, DAY, 500, 40, 20_000));
    }

    @Test
    void defaultsKeepTheSeedYouGive() {
        assertThat(SimulationConfig.defaults(7).seed()).isEqualTo(7);
    }

    // 2. impossible settings are refused when the config is created
    @Test
    void refusesZeroAccounts() {
        assertThatThrownBy(() -> new SimulationConfig(42, DAY, 0, 40, 20_000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesZeroOrders() {
        assertThatThrownBy(() -> new SimulationConfig(42, DAY, 500, 40, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesFewerThanNineSymbols() {
        assertThatThrownBy(() -> new SimulationConfig(42, DAY, 500, 8, 20_000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("9");
    }

    @Test
    void acceptsExactlyNineSymbols() {                       // the boundary: 9 is allowed
        assertThat(new SimulationConfig(42, DAY, 500, 9, 20_000).symbols()).isEqualTo(9);
    }

    @Test
    void refusesANullDay() {
        assertThatThrownBy(() -> new SimulationConfig(42, null, 500, 40, 20_000))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
