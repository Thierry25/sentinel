package com.sentinel.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyTest {

    @Test
    void addsTwoAmountsInTheSameCurrency() {
        var sum = Money.of("10.50", "CAD").plus(Money.of("2.25", "CAD"));
        assertThat(sum).isEqualTo(Money.of("12.75", "CAD"));
    }
}
