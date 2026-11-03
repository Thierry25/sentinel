package com.sentinel.drills.entity;

public record OrderSearch(String accountId,
                          OrderStatus status,
                          boolean marketOnly,
                          Integer minQuantity) {
}
