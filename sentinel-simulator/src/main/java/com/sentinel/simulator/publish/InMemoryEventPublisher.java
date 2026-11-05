package com.sentinel.simulator.publish;

import com.sentinel.model.TradeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class InMemoryEventPublisher implements EventPublisher {
    private final List<TradeEvent> published = new ArrayList<>();

    @Override
    public CompletableFuture <Void> publish(TradeEvent event) {
        published.add(event);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void close() {

    }

    public List<TradeEvent> published() {
        return List.copyOf(published);
    }
}
