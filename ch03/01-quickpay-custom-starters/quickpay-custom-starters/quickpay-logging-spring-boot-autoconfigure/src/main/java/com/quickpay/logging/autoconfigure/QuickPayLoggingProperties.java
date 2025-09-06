package com.quickpay.logging.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Configuration properties for QuickPay logging functionality.
 *
 *
 * <p>Configuration Example:</p>
 * <pre>{@code
 * quickpay:
 *   logging:
 *     enabled: true
 *     ecs:
 *       enabled: true
 *     service:
 *       name: "payment-service"
 *       version: "1.0.0"
 *       environment: "production"
 * }</pre>
 *
 * @param enabled Whether QuickPay logging is enabled (master switch)
 * @param ecs     Elastic Common Schema configuration settings
 * @param service Service identification and metadata configuration
 * @author Somnath Musib
 * @see EcsConfig
 * @see ServiceConfig
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "quickpay.logging")
public record QuickPayLoggingProperties(
        boolean enabled,
        EcsConfig ecs,
        ServiceConfig service
) {

    /**
     * Elastic Common Schema (ECS) configuration for structured logging compliance.
     *
     * <p>Configuration Example:</p>
     * <pre>{@code
     * ecs:
     *   enabled: true
     * }</pre>
     *
     * @param enabled Whether ECS compliance is enabled for log formatting
     * @see <a href="https://www.elastic.co/guide/en/ecs/current/index.html">ECS Specification</a>
     * @since 1.0.0
     */
    public record EcsConfig(
            boolean enabled
    ) {
    }


    /**
     * Service identification and metadata configuration for log correlation.
     * <p>
     * Provides service identity information that gets included in all log events
     * for distributed system tracing, service correlation, and operational visibility.
     * This information is essential for microservices architectures and multi-service deployments.
     * </p>
     *
     * <p>Configuration Example:</p>
     * <pre>{@code
     * service:
     *   name: "payment-gateway-service"
     *   version: "2.1.0"
     *   environment: "production"
     *   metadata:
     *     region: "us-east-1"
     *     cluster: "payment-cluster-prod"
     * }</pre>
     *
     * @param name        Service name for identification in logs and monitoring systems
     * @param version     Service version for deployment tracking and troubleshooting
     * @param environment Deployment environment (e.g., "development", "staging", "production")
     * @param metadata    Additional service metadata for operational context (region, cluster, etc.)
     * @since 1.0.0
     */
    public record ServiceConfig(
            String name,
            String version,
            String environment,
            Map<String, String> metadata
    ) {
        public ServiceConfig {
            metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        }
    }

}