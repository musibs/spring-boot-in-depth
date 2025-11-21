package com.quickpay.payment.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Represents a monetary amount with currency.
 * 
 * This value object encapsulates both the numeric amount and currency,
 * ensuring proper handling of monetary values in payment processing.
 */
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    /**
     * Creates Money from BigDecimal amount and currency.
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    /**
     * Creates Money from double amount and currency.
     */
    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Creates USD Money from double amount.
     */
    public static Money usd(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("USD"));
    }

    /**
     * Creates EUR Money from double amount.
     */
    public static Money eur(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("EUR"));
    }
}