package com.sentinel.simulator.patterns;

import java.util.List;
import java.util.Objects;

public record AnswerKeyEntry(PatternType type,
                             String accountId,
                             String symbol,
                             List<String> eventIds) {
    public AnswerKeyEntry {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(symbol, "symbol");
        eventIds = List.copyOf(eventIds);
        if (eventIds.isEmpty()) {
            throw new IllegalArgumentException("An answer-key row needs at least one event id");
        }
    }
}
