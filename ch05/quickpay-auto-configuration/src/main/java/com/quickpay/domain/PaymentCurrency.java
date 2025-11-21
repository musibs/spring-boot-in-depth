package com.quickpay.domain;

import java.util.Currency;

/**
 * Supported payment currencies for QuickPay.
 * Provides type-safe currency handling.
 */
public enum PaymentCurrency {

    USD(Currency.getInstance("USD")),
    EUR(Currency.getInstance("EUR")),
    GBP(Currency.getInstance("GBP")),
    CAD(Currency.getInstance("CAD")),
    AUD(Currency.getInstance("AUD"));

    private final Currency currency;

    PaymentCurrency(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCode() {
        return currency.getCurrencyCode();
    }

    @Override
    public String toString() {
        return getCode();
    }
}
