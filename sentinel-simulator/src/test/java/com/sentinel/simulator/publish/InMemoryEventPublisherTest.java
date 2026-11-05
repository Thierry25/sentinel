package com.sentinel.simulator.publish;

import com.sentinel.simulator.SimulationConfig;
import com.sentinel.simulator.Simulator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryEventPublisherTest {

    @Test
    void keepsEveryEventInTheOrderItWasPublished() {
        var defaults = SimulationConfig.defaults(42);
        var events = Simulator.standard()
                .run(new SimulationConfig(42, defaults.day(), 50, 40, 300))   // a small day
                .events();

        var publisher = new InMemoryEventPublisher();
        events.forEach(publisher::publish);
        publisher.close();

        assertThat(publisher.published()).containsExactlyElementsOf(events);   // same events, same order
    }
}