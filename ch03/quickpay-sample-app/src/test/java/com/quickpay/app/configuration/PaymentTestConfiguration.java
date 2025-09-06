package com.quickpay.app.configuration;

import com.quickpay.app.service.PaymentProcessingService;
import com.quickpay.observability.events.MetricEventPublisher;
import com.quickpay.observability.metrics.PaymentMetrics;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class PaymentTestConfiguration {
    
    @Bean
    @Primary
    public MeterRegistry testMeterRegistry() {
        return new SimpleMeterRegistry();
    }
    
    @Bean
    @Primary
    public PaymentProcessingService paymentProcessingService() {
        return mock(PaymentProcessingService.class);
    }
    
    @Bean
    @Primary
    public PaymentMetrics paymentMetrics() {
        return mock(PaymentMetrics.class);
    }
    
    @Bean
    @Primary
    public MetricEventPublisher metricEventPublisher() {
        return mock(MetricEventPublisher.class);
    }
}