package com.quickpay.logging.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record PaymentSuccessEvent(

        String transactionId,
        String customerId,
        Money amount,
        String paymentDescription,
        PaymentMethod paymentMethod,

        String authorizationCode,
        Instant transactionTimestamp,
        Duration processingTime,

        String eventCategory,
        String eventType,
        String eventAction,
        String eventOutcome
) {
    public PaymentSuccessEvent {
        Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(paymentDescription, "Payment description cannot be null");
        Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        Objects.requireNonNull(authorizationCode, "Authorization code cannot be null");
        Objects.requireNonNull(transactionTimestamp, "Transaction timestamp time cannot be null");

        Objects.requireNonNull(processingTime, "Processing time cannot be null");

        Objects.requireNonNull(eventCategory, "Event category cannot be null");
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(eventAction, "Event action cannot be null");
        Objects.requireNonNull(eventOutcome, "Event outcome cannot be null");

        if (processingTime.isNegative()) {
            throw new IllegalArgumentException("Processing time cannot be negative");
        }
    }

    public static PaymentSuccessEvent create(
            String transactionId,
            String customerId,
            Money amount,
            String paymentDescription,
            PaymentMethod paymentMethod,
            String authorizationCode,
            Duration processingTime

    ) {
        return new PaymentSuccessEvent(
                transactionId,
                customerId,
                amount,
                paymentDescription,
                paymentMethod,
                authorizationCode,
                Instant.now(),
                processingTime,
                "payment",
                "success",
                "processed",
                "success"
        );
    }

    public boolean isSlowProcessing() {
        return processingTime.compareTo(Duration.ofSeconds(30)) > 0;
    }
}