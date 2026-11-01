package com.sentinel.simulator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IdGeneratorTest {

    @Test
    void firstIdsStartAtOne() {
        var ids = new IdGenerator();

        assertThat(ids.nextEventId()).isEqualTo("E-0000001");
        assertThat(ids.nextOrderId()).isEqualTo("O-0000001");
    }

    @Test
    void eachCallIncrements() {
        var ids = new IdGenerator();
        ids.nextEventId();
        ids.nextEventId();

        assertThat(ids.nextEventId()).isEqualTo("E-0000003");
    }

    @Test
    void eventAndOrderCountersAreIndependent() {
        var ids = new IdGenerator();
        ids.nextEventId();
        ids.nextEventId();

        assertThat(ids.nextOrderId()).isEqualTo("O-0000001");
    }

    @Test
    void twoGeneratorsDoNotShareCounters() {          // the test that catches a "static" counter
        var first = new IdGenerator();
        first.nextEventId();
        first.nextEventId();

        assertThat(new IdGenerator().nextEventId()).isEqualTo("E-0000001");
    }
}
