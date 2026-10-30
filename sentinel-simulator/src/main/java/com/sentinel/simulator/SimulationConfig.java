package com.sentinel.simulator;

import java.time.LocalDate;

public record SimulationConfig(long seed, LocalDate day, int accounts, int symbols, int orders) {

    public SimulationConfig {
        if (accounts <= 0) throw new IllegalArgumentException("Accounts cannot be less or equal to zero");
        if (orders <= 0) throw new IllegalArgumentException("Orders cannot be less or equal to zero");

        if (symbols < 9) {
            throw new IllegalArgumentException("Symbols cannot be greater or equal to 9");
        }

        if (day == null) throw new IllegalArgumentException("Day cannot null");
    }

    public static SimulationConfig defaults(long seed) {
        return new SimulationConfig(seed, LocalDate.of(2026, 10, 27),
                500, 40, 20_000);
    }
}
