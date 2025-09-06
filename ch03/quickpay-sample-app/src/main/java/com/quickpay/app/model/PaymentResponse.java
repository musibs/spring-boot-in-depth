package com.quickpay.app.model;

import java.time.Instant;

public record PaymentResponse(
    String transactionId,
    String status,
    String message,
    double amount,
    String currency,
    Instant processedAt
) {}