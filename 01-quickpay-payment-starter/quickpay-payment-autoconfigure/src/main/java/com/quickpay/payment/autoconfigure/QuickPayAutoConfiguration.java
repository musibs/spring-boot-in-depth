package com.quickpay.payment.autoconfigure;

import com.quickpay.payment.config.QuickPayProperties;
import com.quickpay.payment.repository.InMemoryPaymentRepository;
import com.quickpay.payment.repository.PaymentRepository;
import com.quickpay.payment.service.DefaultPaymentProcessor;
import com.quickpay.payment.service.PaymentProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for QuickPay payment processing.
 * 
 * This configuration class automatically sets up the core payment processing
 * components when the starter is included in the classpath. It provides
 * default implementations that can be overridden by user-defined beans.
 * 
 * The configuration is active when:
 * - quickpay.payment.enabled=true (default)
 * - Required classes are on the classpath
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "quickpay.payment", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(QuickPayProperties.class)
public class QuickPayAutoConfiguration {
    
    /**
     * Provides default in-memory payment repository.
     * Only created if no other PaymentRepository bean exists.
     */
    @Bean
    @ConditionalOnMissingBean
    public PaymentRepository paymentRepository() {
        return new InMemoryPaymentRepository();
    }
    
    /**
     * Provides default payment processor implementation.
     * Only created if no other PaymentProcessor bean exists.
     */
    @Bean
    @ConditionalOnMissingBean
    public PaymentProcessor paymentProcessor(PaymentRepository paymentRepository) {
        return new DefaultPaymentProcessor(paymentRepository);
    }
}