package com.quickpay.payment.domain;

import java.util.Objects;

public record MerchantId(String value) {
    public MerchantId {
        Objects.requireNonNull(value, "Merchant ID value cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Merchant ID value cannot be empty");
        }
    }

    public static MerchantId of(String value) {
        return new MerchantId(value);
    }
}