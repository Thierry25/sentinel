package com.sentinel.model;

public record Account(String accountId,
                      Desk desk) {
    public Account {
        if (accountId == null || accountId.isBlank() || accountId.isEmpty()) throw new IllegalArgumentException("Account id cannot be null");
    }
}
