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

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), other.currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), other.currency);
    }

    public Money times(BigDecimal factor) {
        var result = amount.multiply(factor)
                .setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
        return new Money(result, currency);
    }

    public Money times(long quantity) {
        return times(BigDecimal.valueOf(quantity));
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }


    @Override
    public String toString() {
        return "%s %s".formatted(currency().getCurrencyCode(), amount.toPlainString());
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Same currency required");
        }
    }

}
