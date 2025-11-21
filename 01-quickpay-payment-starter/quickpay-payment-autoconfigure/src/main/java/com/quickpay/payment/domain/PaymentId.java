package com.quickpay.payment.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for payment transactions.
 */
public record PaymentId(String value) {
    public PaymentId {
        Objects.requireNonNull(value, "Payment ID value cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID value cannot be empty");
        }
    }

    /** Generates a new random payment ID. */
    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID().toString());
    }

    /** Creates PaymentId from string value. */
    public static PaymentId of(String value) {
        return new PaymentId(value);
    }
}