package com.quickpay.payment.domain;

import java.util.Objects;

public record CustomerId(String value) {
    public CustomerId {
        Objects.requireNonNull(value, "Customer ID value cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID value cannot be empty");
        }
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }
}