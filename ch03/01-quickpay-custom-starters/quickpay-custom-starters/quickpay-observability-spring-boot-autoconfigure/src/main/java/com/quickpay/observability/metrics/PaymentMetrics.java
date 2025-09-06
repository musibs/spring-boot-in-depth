package com.quickpay.observability.metrics;

import com.quickpay.observability.events.MetricEvent;
import com.quickpay.observability.events.PaymentProcessedMetric;
import com.quickpay.observability.events.PaymentFailedMetric;
import com.quickpay.observability.events.LatencyMetric;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class PaymentMetrics {
    private static final Logger logger = LoggerFactory.getLogger(PaymentMetrics.class);
    
    private final MeterRegistry meterRegistry;
    private final Counter paymentsProcessedCounter;
    private final Counter paymentsFailedCounter;
    private final Timer paymentProcessingTimer;
    
    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.paymentsProcessedCounter = Counter.builder("quickpay_payments_processed_total")
            .description("Total number of processed payments")
            .register(meterRegistry);
        
        this.paymentsFailedCounter = Counter.builder("quickpay_payments_failed_total")
            .description("Total number of failed payments")
            .register(meterRegistry);
            
        this.paymentProcessingTimer = Timer.builder("quickpay_payment_processing_duration")
            .description("Time taken to process payments")
            .register(meterRegistry);
    }
    
    public void recordEvent(MetricEvent event) {
        switch (event) {
            case PaymentProcessedMetric processed -> recordPaymentProcessed(processed);
            case PaymentFailedMetric failed -> recordPaymentFailed(failed);
            case LatencyMetric latency -> recordLatency(latency);
        }
    }
    
    public void recordPaymentProcessed(PaymentProcessedMetric metric) {
        logger.debug("Recording payment processed metric for transaction: {}", metric.transactionId());
        
        Counter.builder("quickpay_payments_processed_total")
            .tag("provider", metric.provider())
            .tag("currency", metric.currency())
            .register(meterRegistry)
            .increment();
            
        Timer.builder("quickpay_payment_processing_duration")
            .tag("provider", metric.provider())
            .tag("currency", metric.currency())
            .register(meterRegistry)
            .record(metric.processingTime());
    }
    
    public void recordPaymentFailed(PaymentFailedMetric metric) {
        logger.debug("Recording payment failed metric for transaction: {}", metric.transactionId());
        
        Counter.builder("quickpay_payments_failed_total")
            .tag("provider", metric.provider())
            .tag("error_code", metric.errorCode())
            .register(meterRegistry)
            .increment();
    }
    
    public void recordLatency(LatencyMetric metric) {
        logger.debug("Recording latency metric for operation: {}", metric.operationName());
        
        Timer.builder("quickpay_operation_duration")
            .description("Duration of QuickPay operations")
            .tag("operation", metric.operationName())
            .tag("provider", metric.provider())
            .tag("success", String.valueOf(metric.successful()))
            .register(meterRegistry)
            .record(metric.latency());
    }
    
    public void incrementPaymentCounter(String provider, String currency) {
        Counter.builder("quickpay_payments_processed_total")
            .tag("provider", provider)
            .tag("currency", currency)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordProcessingTime(Duration duration, String provider, String currency) {
        Timer.builder("quickpay_payment_processing_duration")
            .tag("provider", provider)
            .tag("currency", currency)
            .register(meterRegistry)
            .record(duration);
    }
}