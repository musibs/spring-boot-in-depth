package com.quickpay.logging.autoconfigure;

import com.quickpay.logging.domain.PaymentFailedEvent;
import com.quickpay.logging.domain.PaymentSuccessEvent;
import com.quickpay.logging.formatter.PaymentFailedEventEcsFormatter;
import com.quickpay.logging.formatter.PaymentSuccessEventEcsFormatter;
import com.quickpay.logging.service.PaymentLogger;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for QuickPay logging functionality.
 * Configuration is controlled through the quickpay.logging.* properties.
 */
@AutoConfiguration
@ConditionalOnClass({PaymentSuccessEvent.class, PaymentFailedEvent.class})
@ConditionalOnProperty(prefix = "quickpay.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(QuickPayLoggingProperties.class)
public class QuickPayLoggingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(QuickPayLoggingAutoConfiguration.class);

    /**
     * Creates a PaymentSuccessEventEcsFormatter bean if not already defined.
     * This formatter is used to format payment success events in ECS format.
     *
     * @return a PaymentSuccessEventEcsFormatter instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "quickpay.logging.ecs", name = "enabled", havingValue = "true", matchIfMissing = true)
    PaymentSuccessEventEcsFormatter paymentSuccessEventEcsFormatter() {
        return new PaymentSuccessEventEcsFormatter();
    }

    /**
     * Creates a PaymentFailedEventEcsFormatter bean if not already defined.
     * This formatter is used to format payment failure events in ECS format.
     *
     * @return a PaymentFailedEventEcsFormatter instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "quickpay.logging.ecs", name = "enabled", havingValue = "true", matchIfMissing = true)
    PaymentFailedEventEcsFormatter paymentFailedEventEcsFormatter() {
        return new PaymentFailedEventEcsFormatter();
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentLogger paymentLogger(QuickPayLoggingProperties properties,
                                       ObjectProvider<PaymentSuccessEventEcsFormatter> paymentSuccessEventEcsFormatter,
                                       ObjectProvider<PaymentFailedEventEcsFormatter> paymentFailedEventEcsFormatter) {
        logger.debug("Creating PaymentLogger for service: {}", properties.service().name());
        return new PaymentLogger(
                properties,
                paymentSuccessEventEcsFormatter.getIfAvailable(),
                paymentFailedEventEcsFormatter.getIfAvailable()
        );
    }
}