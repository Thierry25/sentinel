package com.sentinel.simulator;

import com.sentinel.model.TradeEvent;
import com.sentinel.simulator.patterns.AnswerKeyEntry;
import com.sentinel.simulator.universe.Universe;

import java.util.List;
import java.util.Objects;

public record SimulationResult(Universe universe, List<TradeEvent> events, List<AnswerKeyEntry> answerKey) {

    public SimulationResult {
        Objects.requireNonNull(universe, "universe");
        events = List.copyOf(events);
        answerKey = List.copyOf(answerKey);
    }
}
