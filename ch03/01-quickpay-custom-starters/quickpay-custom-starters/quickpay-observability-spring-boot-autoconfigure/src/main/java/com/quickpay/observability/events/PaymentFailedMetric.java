package com.quickpay.observability.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public record PaymentFailedMetric(
    String transactionId,
    Duration processingTime,
    String provider,
    String errorCode,
    String errorMessage,
    Instant timestamp
) implements MetricEvent {
    
    @Override
    public String metricName() { 
        return "payment.failed"; 
    }
    
    @Override
    public Map<String, String> tags() {
        return Map.of(
            "provider", provider,
            "error_code", errorCode,
            "transaction_id", transactionId
        );
    }
}