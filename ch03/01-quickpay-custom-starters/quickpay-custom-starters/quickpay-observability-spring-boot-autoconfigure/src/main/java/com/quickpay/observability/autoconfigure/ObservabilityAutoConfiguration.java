package com.quickpay.observability.autoconfigure;

import com.quickpay.observability.events.MetricEventPublisher;
import com.quickpay.observability.metrics.PaymentMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@EnableConfigurationProperties(ObservabilityProperties.class)
@ConditionalOnProperty(prefix = "quickpay.observability", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ObservabilityAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "quickpay.observability.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
    public PaymentMetrics paymentMetrics(MeterRegistry meterRegistry) {
        return new PaymentMetrics(meterRegistry);
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "quickpay.observability.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
    public MetricEventPublisher metricEventPublisher(ApplicationEventPublisher applicationEventPublisher,
                                                    PaymentMetrics paymentMetrics) {
        return new MetricEventPublisher(applicationEventPublisher, paymentMetrics);
    }
}