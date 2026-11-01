package com.sentinel.simulator.patterns;

import com.sentinel.model.TradeEvent;

import java.util.List;

public record Planted(List<TradeEvent> events,
                      List<AnswerKeyEntry> answers) {

    public Planted {
        events = List.copyOf(events);
        answers = List.copyOf(answers);
    }
}