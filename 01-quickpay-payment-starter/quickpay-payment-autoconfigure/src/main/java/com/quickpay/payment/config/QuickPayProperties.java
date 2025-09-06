package com.quickpay.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for QuickPay payment processing.
 * 
 * Binds to application properties with prefix 'quickpay.payment'
 * and provides type-safe configuration for all payment features.
 * 
 * Example configuration:
 * <pre>
 * quickpay:
 *   payment:
 *     enabled: true
 *     providers:
 *       - name: "stripe"
 *         enabled: true
 *         timeout: 30s
 *     cache:
 *       enabled: true
 *       type: "redis"
 *       ttl: 5m
 * </pre>
 */
@ConfigurationProperties(prefix = "quickpay.payment")
public record QuickPayProperties(
    @DefaultValue("true") boolean enabled,
    List<ProviderConfig> providers,
    SecurityConfig security,
    ObservabilityConfig observability,
    CacheConfig cache,
    RetryConfig retry
) {
    
    /** Configuration for payment providers */
    public record ProviderConfig(
        String name,
        @DefaultValue("true") boolean enabled,
        @DefaultValue("30s") Duration timeout
    ) {}
    
    /** Security-related configuration */
    public record SecurityConfig(
        JwtConfig jwt
    ) {
        /** JWT token configuration */
        public record JwtConfig(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("default-secret") String secret
        ) {}
    }
    
    /** Observability configuration for monitoring */
    public record ObservabilityConfig(
        MetricsConfig metrics,
        TracingConfig tracing,
        LoggingConfig logging
    ) {
        /** Metrics collection configuration */
        public record MetricsConfig(
            @DefaultValue("true") boolean enabled
        ) {}
        
        /** Distributed tracing configuration */
        public record TracingConfig(
            @DefaultValue("true") boolean enabled
        ) {}
        
        /** Logging configuration */
        public record LoggingConfig(
            @DefaultValue("true") boolean correlationId
        ) {}
    }
    
    /** Caching configuration */
    public record CacheConfig(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("caffeine") String type,
        @DefaultValue("5m") Duration ttl
    ) {}
    
    /** Retry policy configuration */
    public record RetryConfig(
        @DefaultValue("3") int maxAttempts,
        @DefaultValue("1s") Duration backoffDelay
    ) {}
}