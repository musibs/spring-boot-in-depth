package com.quickpay.logging.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.logging.structured.StructuredLogFormatter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import com.quickpay.logging.filter.TransactionIdFilter;
import com.quickpay.logging.formatter.QuickPayEcsFormatter;

/**
 * Auto-configuration for QuickPay ECS logging functionality.
 * 
 * This configuration provides:
 * - ECS-compliant structured logging formatter
 * - Transaction correlation across HTTP requests
 * - Automatic service identification in logs
 * - PII masking for security compliance
 * 
 * Configuration is controlled through the quickpay.logging.* properties.
 * 
 * The auto-configuration is activated when:
 * - StructuredLogFormatter is on the classpath (Spring Boot structured logging)
 * - quickpay.logging.enabled=true (default: true)
 */
@AutoConfiguration
@ConditionalOnClass({StructuredLogFormatter.class})
@ConditionalOnProperty(prefix = "quickpay.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(QuickPayLoggingProperties.class)
public class QuickPayLoggingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(QuickPayLoggingAutoConfiguration.class);

    /**
     * Creates the QuickPay ECS-compliant structured log formatter.
     * 
     * This formatter integrates with Spring Boot's structured logging system
     * and provides ECS-compliant JSON output with transaction correlation.
     * 
     * Note: ECS format is ALWAYS active when QuickPay logging is enabled.
     * There is no conditional logic here because ECS cannot be disabled.
     *
     * @param properties the QuickPay logging configuration properties
     * @return a QuickPayEcsFormatter instance configured with service metadata
     */
    @Bean
    @ConditionalOnMissingBean(name = "quickpayEcsFormatter")
    QuickPayEcsFormatter quickpayEcsFormatter(QuickPayLoggingProperties properties) {
        logger.info("Creating QuickPay ECS formatter for service: {} (ECS format is enforced)",
                   properties.getService().getName());

        return new QuickPayEcsFormatter(
            properties.getService().getName(),
            properties.getService().getVersion(),
            properties.getService().getEnvironment(),
            properties.getEcs().isPiiMasking()
        );
    }

    /**
     * Creates and registers the TransactionIdFilter for HTTP request correlation.
     * 
     * This filter extracts transaction IDs from HTTP headers or generates new ones,
     * maintaining transaction context throughout the request lifecycle.
     * 
     * Only registered when:
     * - Running in a web application context
     * - Transaction correlation is enabled
     *
     * @param properties the QuickPay logging configuration properties
     * @return FilterRegistrationBean for the TransactionIdFilter
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "quickpay.logging.correlation", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "transactionIdFilterRegistration")
    FilterRegistrationBean<TransactionIdFilter> transactionIdFilterRegistration(QuickPayLoggingProperties properties) {
        logger.debug("Registering TransactionIdFilter with header: {}", properties.getCorrelation().getHeaderName());

        TransactionIdFilter filter = new TransactionIdFilter(
            properties.getService().getName(),
            properties.getCorrelation().getHeaderName(),
            properties.getCorrelation().isGenerateIfMissing(),
            properties.getCorrelation().isAddToResponse()
        );
        
        FilterRegistrationBean<TransactionIdFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10); // Run early, after security
        registration.setName("transactionIdFilter");
        registration.addUrlPatterns("/*");
        
        return registration;
    }

    /**
     * Configuration for ECS structured logging integration.
     * 
     * This configuration ensures that our QuickPay ECS formatter is automatically
     * used by Spring Boot's structured logging system. ECS format is ALWAYS enforced
     * by QuickPayLoggingSystemInitializer - there are no conditionals here.
     */
    @ConditionalOnClass(name = "org.springframework.boot.logging.structured.StructuredLoggingJsonFormat")
    static class EcsLoggingConfiguration {
        
        private static final Logger logger = LoggerFactory.getLogger(EcsLoggingConfiguration.class);
        
        /**
         * Register the QuickPay ECS formatter with Spring Boot's structured logging system.
         * 
         * ECS format is unconditionally enforced by QuickPayLoggingSystemInitializer.
         * This registration ensures the formatter bean is available for dependency injection.
         */
        @Bean
        @ConditionalOnMissingBean(name = "quickpayStructuredLogFormatter")
        StructuredLogFormatter<?> quickpayStructuredLogFormatter(QuickPayEcsFormatter ecsFormatter) {
            logger.warn("QuickPay ECS formatter registered - ECS format is ENFORCED and CANNOT be overridden");
            return ecsFormatter;
        }
    }
}