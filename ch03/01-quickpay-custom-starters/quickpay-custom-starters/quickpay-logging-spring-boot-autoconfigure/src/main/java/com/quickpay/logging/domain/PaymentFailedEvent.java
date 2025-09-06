package com.quickpay.logging.domain;

import java.util.Objects;

public record PaymentFailedEvent(
        // Core payment identifiers
        String transactionId,
        String customerId,
        Money amount,
        String paymentDescription,
        PaymentMethod paymentMethod,

        // Failure-specific fields
        String errorCode,
        String errorMessage,
        int retryAttempt,
        FailureReason failureReason,

        String eventCategory,
        String eventType,
        String eventAction,
        String eventOutcome
) {
    public PaymentFailedEvent {
        Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(paymentDescription, "Payment description cannot be null");
        Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        Objects.requireNonNull(errorCode, "Error code cannot be null");
        Objects.requireNonNull(errorMessage, "Error message cannot be null");
        Objects.requireNonNull(failureReason, "Failure reason cannot be null");
        Objects.requireNonNull(eventCategory, "Event category cannot be null");
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(eventAction, "Event action cannot be null");
        Objects.requireNonNull(eventOutcome, "Event outcome cannot be null");

        if (retryAttempt < 0) {
            throw new IllegalArgumentException("Retry attempt cannot be negative");
        }
    }

    public static PaymentFailedEvent create(
            String transactionId,
            String customerId,
            Money amount,
            String paymentDescription,
            PaymentMethod paymentMethod,
            String errorCode,
            String errorMessage,
            int retryAttempt,
            FailureReason failureReason
    ) {
        return new PaymentFailedEvent(
                transactionId,
                customerId,
                amount,
                paymentDescription,
                paymentMethod,
                errorCode,
                errorMessage,
                retryAttempt,
                failureReason,
                "payment",
                "failure",
                "rejected",
                "failure"
        );
    }
}