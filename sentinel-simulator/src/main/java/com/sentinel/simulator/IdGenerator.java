package com.sentinel.simulator;

public class IdGenerator {
    private static Long eventId = 1L;
    private static Long orderId = 1L;

    public static String nextEventId() {
        var padded = "E-%07d".formatted(eventId);
        eventId++;
        return padded;
    }

    public static String nextOrderId() {
        var padded = "O-%07d".formatted(orderId);
        orderId++;
        return padded;
    }
}
