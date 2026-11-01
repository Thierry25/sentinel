package com.sentinel.simulator;

public class IdGenerator {
    private Long eventId = 1L;
    private Long orderId = 1L;

    public String nextEventId() {
        var padded = "E-%07d".formatted(eventId);
        eventId++;
        return padded;
    }

    public String nextOrderId() {
        var padded = "O-%07d".formatted(orderId);
        orderId++;
        return padded;
    }
}
