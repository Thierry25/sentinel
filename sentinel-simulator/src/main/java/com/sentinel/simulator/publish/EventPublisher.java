package com.sentinel.simulator.publish;

import com.sentinel.model.TradeEvent;

import java.util.concurrent.CompletableFuture;

public interface EventPublisher extends AutoCloseable {
    CompletableFuture<Void> publish(TradeEvent event);

    @Override
    void close();
}
