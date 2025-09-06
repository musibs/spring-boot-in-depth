package com.quickpay.observability.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Set;

/**
 * Configuration properties for QuickPay observability functionality.
 * <p>
 * Integrates Micrometer metrics, distributed tracing, and monitoring capabilities
 * with automatic transaction correlation for comprehensive system observability.
 * <p>
 * Example configuration:
 * <pre>{@code
 * quickpay:
 *   observability:
 *     enabled: true
 *     metrics:
 *       enabled: true
 *       prefix: "quickpay"
 *       enabled-metrics: ["payments", "transfers", "health"]
 *     tracing:
 *       enabled: true
 *       service-name: "payment-service"
 *       sampling-rate: 0.1
 *     health:
 *       enabled: true
 *       timeout: PT30S
 *       check-interval: PT1M
 * }</pre>
 *
 * @param enabled whether QuickPay observability features are enabled
 * @param metrics configuration for application metrics collection
 * @param tracing configuration for distributed tracing
 * @param health configuration for health check monitoring
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "quickpay.observability")
public record ObservabilityProperties(
    boolean enabled,
    MetricsConfig metrics,
    TracingConfig tracing,
    HealthConfig health
) {
    
    /**
     * Configuration for application metrics collection using Micrometer.
     * <p>
     * Controls which metrics are collected and how they are named and tagged.
     * Supports selective metric enablement for performance optimization.
     *
     * @param enabled whether metrics collection is enabled
     * @param prefix the prefix to apply to all QuickPay metric names
     * @param enabledMetrics set of metric types to collect (e.g., "payments", "transfers", "health")
     */
    public record MetricsConfig(
        boolean enabled,
        String prefix,
        Set<String> enabledMetrics
    ) {
        public MetricsConfig {
            enabledMetrics = enabledMetrics != null ? Set.copyOf(enabledMetrics) : Set.of();
        }
    }
    
    /**
     * Configuration for distributed tracing across QuickPay services.
     * <p>
     * Enables automatic trace correlation and span creation for tracking
     * requests across multiple microservices and external systems.
     *
     * @param enabled whether distributed tracing is enabled
     * @param serviceName the name to identify this service in trace data
     * @param samplingRate the percentage of requests to trace (0.0 to 1.0)
     */
    public record TracingConfig(
        boolean enabled,
        String serviceName,
        double samplingRate
    ) {}
    
    /**
     * Configuration for health check monitoring and readiness probes.
     * <p>
     * Configures automated health checks for dependencies and system components
     * with customizable timeouts and check intervals.
     *
     * @param enabled whether health check monitoring is enabled
     * @param timeout maximum time to wait for health check responses
     * @param checkInterval how frequently to perform health checks
     */
    public record HealthConfig(
        boolean enabled,
        Duration timeout,
        Duration checkInterval
    ) {}
    
    public static ObservabilityProperties defaultConfig() {
        return new ObservabilityProperties(
            true,
            new MetricsConfig(
                true, 
                "quickpay", 
                Set.of("payments", "transfers", "health")
            ),
            new TracingConfig(
                true, 
                "quickpay-service", 
                1.0
            ),
            new HealthConfig(
                true, 
                Duration.ofSeconds(30), 
                Duration.ofMinutes(1)
            )
        );
    }
}