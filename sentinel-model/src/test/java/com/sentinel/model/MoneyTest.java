package com.sentinel.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
        assertThrows(IllegalArgumentException.class, () -> Money.of("10", "XYZ"));
    }

    @Test
    void shouldThrowExceptionOnOnlyNullAmount() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null, "XYZ"));
    }

    @Test
    void shouldThrowExceptionOnOnlyCurrency() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("10", null));
    }

    @Test
    void shouldThrowExceptionOnBothNullValues() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null, null));
    }

    @Test
    void subtractOneAmountFromAnotherSameCurrency() {
        var subs = Money.of("10", "CAD").minus(Money.of("5", "CAD"));
        assertThat(subs).isEqualTo(Money.of("5", "CAD"));
    }

    @Test
    void subtractOneAmountFromAnother() {
        var subs = Money.of("5.00", "CAD").minus(Money.of("7.50", "CAD"));
        var result = Money.of("-2.50", "CAD");
        assertThat(subs).isEqualTo(result);
        assertTrue(subs.isNegative());
    }

    @Test
    void shouldThrowExceptionOnSubtractingDifferentCurrencies() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("10", "CAD")
                .minus(Money.of("2.25", "USD")));
    }

    @Test
    void canDetermineThatAValueIsNegative() {
        assertTrue(Money.of("-2.50", "CAD").isNegative());
    }


    @Test
    void priceTimesQuantityGivesTheNotional() {
        assertThat(Money.of("230.00", "USD").times(100)).isEqualTo(Money.of("23000.00", "USD"));
    }

    @Test
    void timesAFactorUsesBankersRounding() {
        assertThat(Money.of("0.25", "CAD").times(new BigDecimal("0.5"))).isEqualTo(Money.of("0.12", "CAD"));
        assertThat(Money.of("0.35", "CAD").times(new BigDecimal("0.5"))).isEqualTo(Money.of("0.18", "CAD"));
    }

    @Test
    void shouldBeAbleToDetermineWhichValueIsGreater() {
        assertTrue(Money.of("7.50", "CAD").isGreaterThan(Money.of("5", "CAD")));
    }

    @Test
    void equalValuesShouldReturnFalseWhenDeterminingWhichIsGreater() {
        assertFalse(Money.of("7.50", "CAD").isGreaterThan(Money.of("7.50", "CAD")));
    }

    @Test
    void printsTwoDecimalsForCad() {
        assertThat(Money.of("12.7", "CAD").toString()).isEqualTo("CAD 12.70");
    }

    @Test void scaleDoesNotMatterForEquality() {
        assertThat(Money.of("2.50", "CAD").plus(Money.of("0.50", "CAD")))
                .isEqualTo(Money.of("3", "CAD"));
    }
    @Test void printsZeroDecimalsForJpyAndThreeForKwd() {
        assertThat(Money.of("1500", "JPY")).hasToString("JPY 1500");
        assertThat(Money.of("1", "KWD")).hasToString("KWD 1.000");
    }

    @Test void refusesToCompareDifferentCurrencies() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("10", "USD").isGreaterThan(Money.of("10", "CAD")));
    }

    @Test void refusesCurrenciesWithoutMinorUnit()   {
        assertThrows(IllegalArgumentException.class, () -> Money.of("1", "XAU"));
    }

    @Test
    void zeroHasTheCurrenciesDecimals() {
        assertThat(Money.zero(Currency.getInstance("USD"))).hasToString("USD 0.00");
        assertThat(Money.zero(Currency.getInstance("JPY"))).hasToString("JPY 0");
    }
}
