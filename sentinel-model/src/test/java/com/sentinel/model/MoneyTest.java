package com.sentinel.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MoneyTest {

    @Test
    void addsTwoAmountsInTheSameCurrency() {
        var sum = Money.of("10.50", "CAD").plus(Money.of("2.25", "CAD"));
        assertThat(sum).isEqualTo(Money.of("12.75", "CAD"));
    }

    @Test
    void addTwoAmountsOfDifferentCurrenciesThrowException() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("10.50", "CAD")
                .plus(Money.of("2.25", "USD")));
    }

    @Test
    void shouldThrowExceptionWhenCurrencyShouldNotHaveDecimalsOtherThanZero() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("1.5", "JPY"));
    }

    @Test
    void shouldThrowExceptionOnTooManyDecimalsProvided() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("10.6792", "CAD"));
    }

    @Test
    void shouldThrowExceptionOnUnknownCurrency() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("10.6792", "XYZ"));
    }

    @Test
    void shouldThrowExceptionOnOnlyNullAmount() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null, "XYZ"));
    }

    @Test
    void shouldThrowExceptionOnOnlyCurrency() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null, "XYZ"));
    }

    @Test
    void shouldThrowExceptionOnBothNullValues() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null, null));
    }
}
