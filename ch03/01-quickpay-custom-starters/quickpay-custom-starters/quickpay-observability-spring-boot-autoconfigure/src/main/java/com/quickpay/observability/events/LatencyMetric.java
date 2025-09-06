package com.quickpay.observability.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public record LatencyMetric(
    String operationName,
    Duration latency,
    String provider,
    boolean successful,
    Instant timestamp
) implements MetricEvent {
    
    @Override
    public String metricName() { 
        return "operation.latency"; 
    }
    
    @Override
    public Map<String, String> tags() {
        return Map.of(
            "operation", operationName,
            "provider", provider,
            "success", String.valueOf(successful)
        );
    }
}