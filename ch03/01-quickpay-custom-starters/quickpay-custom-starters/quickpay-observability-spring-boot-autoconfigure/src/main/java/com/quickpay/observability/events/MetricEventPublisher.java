package com.quickpay.observability.events;

import com.quickpay.observability.metrics.PaymentMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MetricEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(MetricEventPublisher.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PaymentMetrics paymentMetrics;
    
    public MetricEventPublisher(ApplicationEventPublisher applicationEventPublisher, 
                               PaymentMetrics paymentMetrics) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.paymentMetrics = paymentMetrics;
    }
    
    public void publishEvent(MetricEvent event) {
        logger.debug("Publishing metric event: {}", event.metricName());
        
        // Record metrics immediately
        paymentMetrics.recordEvent(event);
        
        // Also publish as Spring application event for other listeners
        applicationEventPublisher.publishEvent(event);
    }
    
    public void publishPaymentProcessed(String transactionId, 
                                      java.time.Duration processingTime,
                                      String provider, 
                                      String currency, 
                                      double amount) {
        var event = new PaymentProcessedMetric(
            transactionId, processingTime, provider, currency, amount, java.time.Instant.now()
        );
        publishEvent(event);
    }
    
    public void publishPaymentFailed(String transactionId, 
                                   java.time.Duration processingTime,
                                   String provider, 
                                   String errorCode, 
                                   String errorMessage) {
        var event = new PaymentFailedMetric(
            transactionId, processingTime, provider, errorCode, errorMessage, java.time.Instant.now()
        );
        publishEvent(event);
    }
    
    public void publishLatencyMetric(String operationName, 
                                   java.time.Duration latency, 
                                   String provider, 
                                   boolean successful) {
        var event = new LatencyMetric(
            operationName, latency, provider, successful, java.time.Instant.now()
        );
        publishEvent(event);
    }
}