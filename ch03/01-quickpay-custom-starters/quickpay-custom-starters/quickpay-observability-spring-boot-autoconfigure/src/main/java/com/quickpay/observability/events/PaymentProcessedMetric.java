package com.quickpay.observability.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public record PaymentProcessedMetric(
    String transactionId,
    Duration processingTime,
    String provider,
    String currency,
    double amount,
    Instant timestamp
) implements MetricEvent {
    
    @Override
    public String metricName() { 
        return "payment.processed"; 
    }
    
    @Override
    public Map<String, String> tags() {
        return Map.of(
            "provider", provider,
            "currency", currency,
            "transaction_id", transactionId
        );
    }
}