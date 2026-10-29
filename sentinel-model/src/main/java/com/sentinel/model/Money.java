package com.sentinel.model;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public static Money of(String amount, String currencyCode) {
        if (amount == null || currencyCode == null) {
            throw new IllegalArgumentException("Cannot have null values");
        }

        var bigDecimalFromAmount = new BigDecimal(amount);
        int decimalCount = Math.max(0, bigDecimalFromAmount.stripTrailingZeros().scale());

        var currency = Currency.getInstance(currencyCode);
        var currencyAllowedDecimals = currency.getDefaultFractionDigits();

        if (decimalCount > currencyAllowedDecimals) {
            throw new IllegalArgumentException("Too many decimals provided for this currency");
        }
        return new Money(bigDecimalFromAmount, currency);
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
            throw new IllegalArgumentException("Cannot add two different currencies");
        }

        return new Money(this.amount.multiply(other.amount), other.currency);
    }

    public boolean isGreaterThan(Money other) {return false;}

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public String toString() {
        return "%s %f".formatted(currency().getCurrencyCode(), amount);
    }

    private boolean isDifferentCurrency(Money other) {
        return !this.currency.equals(other.currency);
    }

}
