package com.quickpay.app.model;

import com.quickpay.logging.domain.Money;

import java.time.Instant;

public record PaymentResponse(
    String transactionId,
    String status,
    String message,
    Money money,
    Instant processedAt
) {}