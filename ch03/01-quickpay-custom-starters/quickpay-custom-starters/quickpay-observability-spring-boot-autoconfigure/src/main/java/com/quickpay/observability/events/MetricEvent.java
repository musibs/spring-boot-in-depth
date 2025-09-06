package com.quickpay.observability.events;

import java.time.Instant;
import java.util.Map;

public sealed interface MetricEvent permits 
    PaymentProcessedMetric, PaymentFailedMetric, LatencyMetric {
    
    String metricName();
    Map<String, String> tags();
    Instant timestamp();
}