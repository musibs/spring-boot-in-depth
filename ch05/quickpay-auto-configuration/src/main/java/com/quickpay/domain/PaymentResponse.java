package com.quickpay.domain;

import java.time.Instant;

public record PaymentResponse(
        String transactionId,
        PaymentStatus status,
        PaymentProvider provider,
        Instant timestamp,
        String message
) {

    /**
     * Payment status enum
     */
    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

    /**
     * Compact constructor with validation
     */
    public PaymentResponse {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Provider is required");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    /**
     * Factory method for creating a successful payment response
     */
    public static PaymentResponse success(String transactionId, PaymentProvider provider) {
        return new PaymentResponse(transactionId, PaymentStatus.SUCCESS, provider, Instant.now(), null);
    }

    /**
     * Factory method for creating a failed payment response
     */
    public static PaymentResponse failed(String transactionId, PaymentProvider provider, String errorMessage) {
        return new PaymentResponse(transactionId, PaymentStatus.FAILED, provider, Instant.now(), errorMessage);
    }
}
