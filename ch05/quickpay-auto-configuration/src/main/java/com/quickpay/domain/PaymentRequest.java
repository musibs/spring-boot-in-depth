package com.quickpay.domain;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Immutable payment request representing a payment transaction to be processed.
 * Uses Java Record for concise value object implementation.
 *
 * @param amount      the payment amount (must be positive)
 * @param currency    the payment currency
 * @param customerId  unique identifier for the customer
 * @param metadata    additional metadata for the payment (e.g., order ID, description)
 */
public record PaymentRequest(
        BigDecimal amount,
        PaymentCurrency currency,
        String customerId,
        Map<String, String> metadata
) {

    /**
     * Compact constructor with validation
     */
    public PaymentRequest {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        // Create defensive copy of metadata to ensure immutability
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Factory method for creating a simple payment request
     */
    public static PaymentRequest of(BigDecimal amount, PaymentCurrency currency, String customerId) {
        return new PaymentRequest(amount, currency, customerId, Map.of());
    }
}
