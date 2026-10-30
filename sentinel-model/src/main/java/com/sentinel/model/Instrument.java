package com.sentinel.model;

public record Instrument(String symbol,
                         String sector,
                         Money referencePrice) {
    public Instrument {
        if (referencePrice == null) throw new IllegalArgumentException("Reference Price cannot be null");
    }
}
