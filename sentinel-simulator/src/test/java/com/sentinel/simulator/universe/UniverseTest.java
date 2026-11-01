package com.sentinel.simulator.universe;

import com.sentinel.simulator.SimulationConfig;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UniverseTest {

    private final Universe universe = new UniverseFactory().create(SimulationConfig.defaults(42), new Random(42));

    @Test
    void foreignSymbolIsNeverHomeNorRestricted() {
        var random = new Random(7);
        var home = universe.homeSymbols().get("A0001");

        for (int i = 0; i < 1_000; i++) {
            var symbol = universe.foreignSymbolFor("A0001", random);
            assertThat(home).doesNotContain(symbol);
            assertThat(universe.restrictedSymbols()).doesNotContain(symbol);
        }
    }

    @Test
    void referencePriceOfAnUnknownSymbolIsRefused() {
        assertThatThrownBy(() -> universe.referencePrice("NOPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NOPE");
    }

    @Test
    void restrictedSymbolsInOrderAreSorted() {
        assertThat(universe.restrictedSymbolsInOrder()).isSorted().hasSize(3);
    }
}