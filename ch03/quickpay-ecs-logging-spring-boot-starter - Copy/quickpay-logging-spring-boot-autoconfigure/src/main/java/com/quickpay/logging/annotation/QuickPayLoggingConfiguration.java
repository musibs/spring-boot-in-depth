package com.quickpay.logging.annotation;

import com.quickpay.logging.features.AsyncLoggingEnhancer;
import com.quickpay.logging.features.AuditLoggingEnhancer;
import com.quickpay.logging.features.ErrorCategorizationEnhancer;
import com.quickpay.logging.features.MetricsCollectionEnhancer;
import com.quickpay.logging.features.PerformanceMonitoringEnhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.Map;

/**
 * Configuration class that processes @EnableQuickPayLogging annotation
 * and sets up additional logging features based on annotation parameters.
 * 
 * This works alongside the automatic ECS enforcement to provide
 * enhanced logging capabilities.
 */
@Configuration
public class QuickPayLoggingConfiguration implements ImportAware {
    
    private static final Logger logger = LoggerFactory.getLogger(QuickPayLoggingConfiguration.class);
    
    private AnnotationAttributes enableQuickPayLogging;
    
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableQuickPayLogging = AnnotationAttributes.fromMap(
            importMetadata.getAnnotationAttributes(EnableQuickPayLogging.class.getName(), false));
        
        if (this.enableQuickPayLogging == null) {
            throw new IllegalArgumentException(
                "@EnableQuickPayLogging is not present on importing class " + importMetadata.getClassName());
        }
        
        logFeatureActivation();
    }
    
    /**
     * Async logging enhancer for high-performance logging.
     */
    @Bean
    public AsyncLoggingEnhancer asyncLoggingEnhancer() {
        boolean enabled = enableQuickPayLogging.getBoolean("enableAsyncLogging");
        return new AsyncLoggingEnhancer(enabled);
    }
    
    /**
     * Audit logging enhancer for compliance requirements.
     */
    @Bean
    public AuditLoggingEnhancer auditLoggingEnhancer() {
        boolean enabled = enableQuickPayLogging.getBoolean("auditingEnabled");
        return new AuditLoggingEnhancer(enabled);
    }
    
    /**
     * Performance monitoring enhancer for method-level tracking.
     */
    @Bean
    public PerformanceMonitoringEnhancer performanceMonitoringEnhancer() {
        boolean enabled = enableQuickPayLogging.getBoolean("performanceMonitoring");
        return new PerformanceMonitoringEnhancer(enabled);
    }
    
    /**
     * Error categorization enhancer for intelligent error handling.
     */
    @Bean
    public ErrorCategorizationEnhancer errorCategorizationEnhancer() {
        boolean enabled = enableQuickPayLogging.getBoolean("errorCategorizationEnabled");
        return new ErrorCategorizationEnhancer(enabled);
    }
    
    /**
     * Metrics collection enhancer for structured metrics export.
     */
    @Bean
    public MetricsCollectionEnhancer metricsCollectionEnhancer() {
        boolean enabled = enableQuickPayLogging.getBoolean("metricsEnabled");
        return new MetricsCollectionEnhancer(enabled);
    }
    
    /**
     * Enhanced configuration properties that merge annotation values with properties file.
     */
    @Bean
    public EnhancedLoggingProperties enhancedLoggingProperties() {
        return EnhancedLoggingProperties.builder()
            .serviceName(enableQuickPayLogging.getString("serviceName"))
            .environment(enableQuickPayLogging.getString("environment"))
            .version(enableQuickPayLogging.getString("version"))
            .correlationHeaders(Arrays.asList(enableQuickPayLogging.getStringArray("correlationHeaders")))
            .sensitiveFields(Arrays.asList(enableQuickPayLogging.getStringArray("sensitiveFields")))
            .customTags(parseCustomTags(enableQuickPayLogging.getStringArray("customTags")))
            .build();
    }
    
    /**
     * Log which features are being activated.
     */
    private void logFeatureActivation() {
        logger.info("@EnableQuickPayLogging detected - activating enhanced logging features");
        
        if (enableQuickPayLogging.getBoolean("enableAsyncLogging")) {
            logger.info("Async Logging: ENABLED - High-performance logging activated");
        }
        
        if (enableQuickPayLogging.getBoolean("auditingEnabled")) {
            logger.info("Audit Logging: ENABLED - Compliance audit trail activated");
        }
        
        if (enableQuickPayLogging.getBoolean("performanceMonitoring")) {
            logger.info("Performance Monitoring: ENABLED - Method-level tracking activated");
        }
        
        if (enableQuickPayLogging.getBoolean("errorCategorizationEnabled")) {
            logger.info("Error Categorization: ENABLED - Intelligent error analysis activated");
        }
        
        if (enableQuickPayLogging.getBoolean("metricsEnabled")) {
            logger.info("Metrics Collection: ENABLED - Structured metrics export activated");
        }
        
        String[] correlationHeaders = enableQuickPayLogging.getStringArray("correlationHeaders");
        if (correlationHeaders.length > 0) {
            logger.info("Additional Correlation Headers: {}", Arrays.toString(correlationHeaders));
        }
        
        String[] sensitiveFields = enableQuickPayLogging.getStringArray("sensitiveFields");
        if (sensitiveFields.length > 0) {
            logger.info("Additional Sensitive Fields: {} fields configured for masking", sensitiveFields.length);
        }
        
        String[] customTags = enableQuickPayLogging.getStringArray("customTags");
        if (customTags.length > 0) {
            logger.info("Custom Tags: {} tags configured", customTags.length);
        }
    }
    
    /**
     * Parse custom tags from "key=value" format.
     */
    private Map<String, String> parseCustomTags(String[] customTags) {
        return Arrays.stream(customTags)
            .filter(tag -> tag.contains("="))
            .map(tag -> tag.split("=", 2))
            .filter(parts -> parts.length == 2)
            .collect(java.util.stream.Collectors.toMap(
                parts -> parts[0].trim(),
                parts -> parts[1].trim(),
                (existing, replacement) -> replacement // Handle duplicates
            ));
    }
    
    /**
     * Enhanced logging properties that combine annotation config with properties file.
     */
    public static class EnhancedLoggingProperties {
        private final String serviceName;
        private final String environment;
        private final String version;
        private final java.util.List<String> correlationHeaders;
        private final java.util.List<String> sensitiveFields;
        private final Map<String, String> customTags;
        
        private EnhancedLoggingProperties(Builder builder) {
            this.serviceName = builder.serviceName;
            this.environment = builder.environment;
            this.version = builder.version;
            this.correlationHeaders = builder.correlationHeaders;
            this.sensitiveFields = builder.sensitiveFields;
            this.customTags = builder.customTags;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public String getEnvironment() { return environment; }
        public String getVersion() { return version; }
        public java.util.List<String> getCorrelationHeaders() { return correlationHeaders; }
        public java.util.List<String> getSensitiveFields() { return sensitiveFields; }
        public Map<String, String> getCustomTags() { return customTags; }
        
        public static class Builder {
            private String serviceName = "";
            private String environment = "";
            private String version = "";
            private java.util.List<String> correlationHeaders = java.util.Collections.emptyList();
            private java.util.List<String> sensitiveFields = java.util.Collections.emptyList();
            private Map<String, String> customTags = java.util.Collections.emptyMap();
            
            public Builder serviceName(String serviceName) {
                this.serviceName = serviceName;
                return this;
            }
            
            public Builder environment(String environment) {
                this.environment = environment;
                return this;
            }
            
            public Builder version(String version) {
                this.version = version;
                return this;
            }
            
            public Builder correlationHeaders(java.util.List<String> correlationHeaders) {
                this.correlationHeaders = correlationHeaders;
                return this;
            }
            
            public Builder sensitiveFields(java.util.List<String> sensitiveFields) {
                this.sensitiveFields = sensitiveFields;
                return this;
            }
            
            public Builder customTags(Map<String, String> customTags) {
                this.customTags = customTags;
                return this;
            }
            
            public EnhancedLoggingProperties build() {
                return new EnhancedLoggingProperties(this);
            }
        }
    }
}