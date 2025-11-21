package com.quickpay.logging.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable QuickPay logging with ECS enforcement and additional features.
 * 
 * <p><strong>🔒 ECS Enforcement:</strong> ECS format is automatically enforced regardless
 * of this annotation. This annotation provides additional configuration and features
 * on top of the base ECS logging infrastructure.</p>
 * 
 * <h2>Basic Usage:</h2>
 * <pre>{@code
 * @EnableQuickPayLogging
 * @SpringBootApplication
 * public class PaymentApplication {
 * }
 * }</pre>
 * 
 * <h3>Advanced Configuration:</h3>
 * <pre>{@code
 * @EnableQuickPayLogging(
 *     serviceName = "payment-gateway",
 *     environment = "production",
 *     enableAsyncLogging = true,
 *     auditingEnabled = true,
 *     performanceMonitoring = true,
 *     correlationHeaders = {"X-Request-ID", "X-Trace-ID"},
 *     sensitiveFields = {"password", "ssn", "creditCard"}
 * )
 * @SpringBootApplication
 * public class PaymentApplication {
 * }
 * }</pre>
 * 
 * <h3>Features Enabled by This Annotation:</h3>
 * <ul>
 *   <li><strong>Service Registration:</strong> Auto-registers service in distributed logging</li>
 *   <li><strong>Audit Logging:</strong> Automatic audit trail for compliance</li>
 *   <li><strong>Performance Monitoring:</strong> Method-level performance tracking</li>
 *   <li><strong>Async Logging:</strong> High-performance async logging for high-throughput services</li>
 *   <li><strong>Custom Correlation:</strong> Additional correlation headers beyond standard transaction ID</li>
 *   <li><strong>Security Enhancement:</strong> Advanced PII detection and masking</li>
 *   <li><strong>Error Categorization:</strong> Automatic error classification and alerting</li>
 * </ul>
 * 
 * @author QuickPay Platform Team
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(QuickPayLoggingConfiguration.class)
public @interface EnableQuickPayLogging {
    
    /**
     * Service name for logging identification.
     * Overrides quickpay.logging.service.name property if specified.
     * 
     * @return the service name
     */
    String serviceName() default "";
    
    /**
     * Environment designation (development, staging, production).
     * Overrides quickpay.logging.service.environment property if specified.
     * 
     * @return the environment name
     */
    String environment() default "";
    
    /**
     * Service version for deployment tracking.
     * Overrides quickpay.logging.service.version property if specified.
     * 
     * @return the service version
     */
    String version() default "";
    
    /**
     * Enable asynchronous logging for high-performance applications.
     * Useful for services with high transaction volumes.
     * 
     * <p><strong>Benefits:</strong></p>
     * <ul>
     *   <li>Reduced latency impact from logging operations</li>
     *   <li>Better throughput for high-volume services</li>
     *   <li>Automatic batching and buffering</li>
     * </ul>
     * 
     * @return true to enable async logging
     */
    boolean enableAsyncLogging() default false;
    
    /**
     * Enable comprehensive audit logging for compliance requirements.
     * 
     * <p><strong>Features:</strong></p>
     * <ul>
     *   <li>Automatic audit trail for all transactions</li>
     *   <li>Compliance-ready log format</li>
     *   <li>Immutable audit records</li>
     *   <li>Integration with audit management systems</li>
     * </ul>
     * 
     * @return true to enable audit logging
     */
    boolean auditingEnabled() default false;
    
    /**
     * Enable automatic performance monitoring and alerting.
     * 
     * <p><strong>Capabilities:</strong></p>
     * <ul>
     *   <li>Method execution time tracking</li>
     *   <li>Slow query detection</li>
     *   <li>Memory usage monitoring</li>
     *   <li>Performance trend analysis</li>
     * </ul>
     * 
     * @return true to enable performance monitoring
     */
    boolean performanceMonitoring() default false;
    
    /**
     * Additional correlation headers to extract and propagate.
     * These are added to the standard X-Transaction-ID correlation.
     * 
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Integration with external tracing systems</li>
     *   <li>Multi-tenant correlation (X-Tenant-ID)</li>
     *   <li>Regional routing correlation (X-Region)</li>
     *   <li>Partner integration tracking (X-Partner-ID)</li>
     * </ul>
     * 
     * @return array of additional header names
     */
    String[] correlationHeaders() default {};
    
    /**
     * Additional sensitive field names for enhanced PII masking.
     * These are added to the default sensitive fields.
     * 
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>Domain-specific fields: "accountNumber", "routingNumber"</li>
     *   <li>Business-specific PII: "customerTaxId", "socialSecurityNumber"</li>
     *   <li>Custom sensitive data: "apiKey", "authToken"</li>
     * </ul>
     * 
     * @return array of additional sensitive field names
     */
    String[] sensitiveFields() default {};
    
    /**
     * Enable automatic error categorization and alerting.
     * 
     * <p><strong>Features:</strong></p>
     * <ul>
     *   <li>Intelligent error classification (business vs technical)</li>
     *   <li>Automatic alert generation for critical errors</li>
     *   <li>Error pattern detection and analysis</li>
     *   <li>Integration with incident management systems</li>
     * </ul>
     * 
     * @return true to enable error categorization
     */
    boolean errorCategorizationEnabled() default false;
    
    /**
     * Enable structured metrics collection and export.
     * 
     * <p><strong>Metrics Collected:</strong></p>
     * <ul>
     *   <li>Transaction throughput and latency</li>
     *   <li>Error rates and patterns</li>
     *   <li>Resource utilization</li>
     *   <li>Business KPIs and SLAs</li>
     * </ul>
     * 
     * @return true to enable metrics collection
     */
    boolean metricsEnabled() default false;
    
    /**
     * Custom tags to be added to all log entries from this service.
     * Useful for service categorization and filtering.
     * 
     * <p><strong>Format:</strong> "key=value" pairs</p>
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>"team=payments"</li>
     *   <li>"domain=financial"</li>
     *   <li>"criticality=high"</li>
     * </ul>
     * 
     * @return array of custom tags
     */
    String[] customTags() default {};
}