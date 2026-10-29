package com.sentinel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || currency == null) throw new IllegalArgumentException("Cannot have null values");
        int digits = currency.getDefaultFractionDigits();
        if (digits < 0) {
            throw new IllegalArgumentException(currency + " is not a payment currency");
        }
        try {
            amount = amount.setScale(digits, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Too many decimals for %s: %s".formatted(currency, amount.toPlainString()), e);
        }
    }
    public static Money of(String amount, String currencyCode) {
        if (amount == null || currencyCode == null) {
            throw new IllegalArgumentException("Cannot have null values");
        }
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public Money plus(Money other) {
        if (isDifferentCurrency(other)) {
            throw new IllegalArgumentException("Cannot add two different currencies");
        }
        return new Money(this.amount.add(other.amount), other.currency);
    }

    public Money minus(Money other) {
        if (isDifferentCurrency(other)) {
            throw new IllegalArgumentException("Cannot add two different currencies");
        }

        return new Money(this.amount.subtract(other.amount), other.currency);
    }

    public Money times(Money other) {
        if (isDifferentCurrency(other)) {
            throw new IllegalArgumentException("Cannot remove two different currencies");
        }

        return new Money(this.amount.multiply(other.amount).setScale( other.currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN), other.currency);
    }

    public boolean isGreaterThan(Money other) {
        if (isDifferentCurrency(other)) {
            throw new IllegalArgumentException("Cannot add two different currencies");
        }

        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public String toString() {
        return "%s %s".formatted(currency().getCurrencyCode(), amount.toPlainString());
    }

    private boolean isDifferentCurrency(Money other) {
        return !this.currency.equals(other.currency);
    }

}
