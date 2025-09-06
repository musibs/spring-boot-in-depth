package com.quickpay.app.model;

import java.util.Objects;

public record PaymentRequest(
    double amount,
    String currency,
    String customerId,
    String merchantId,
    String description
) {
    public PaymentRequest {
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(merchantId, "Merchant ID cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        if (currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }
    }
}