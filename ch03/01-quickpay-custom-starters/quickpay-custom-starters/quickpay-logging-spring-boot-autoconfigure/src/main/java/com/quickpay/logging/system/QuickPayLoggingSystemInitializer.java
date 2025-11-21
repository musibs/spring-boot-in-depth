package com.quickpay.logging.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment post-processor that automatically enforces ECS structured logging
 * for all QuickPay applications. This ensures consistent, non-overrideable 
 * logging format across all services.
 * 
 * This post-processor runs early in the Spring Boot lifecycle to configure
 * logging system properties before the logging system is initialized.
 */
public class QuickPayLoggingSystemInitializer implements EnvironmentPostProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(QuickPayLoggingSystemInitializer.class);
    
    private static final String QUICKPAY_LOGGING_PREFIX = "quickpay.logging";
    private static final String PROPERTY_SOURCE_NAME = "quickpay-ecs-logging-enforcer";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isQuickPayLoggingEnabled(environment)) {
            try {
                enforceEcsLogging(environment);
                logger.debug("QuickPay ECS logging enforcement applied successfully");
            } catch (Exception e) {
                logger.warn("Failed to enforce QuickPay ECS logging: {}", e.getMessage());
                logger.debug("QuickPay ECS logging enforcement error details", e);
            }
        } else {
            logger.debug("QuickPay logging is disabled, skipping ECS enforcement");
        }
    }
    
    /**
     * Checks if QuickPay logging is enabled based on configuration properties.
     */
    private boolean isQuickPayLoggingEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty(QUICKPAY_LOGGING_PREFIX + ".enabled", Boolean.class, true);
    }
    
    /**
     * Enforces ECS structured logging by automatically setting Spring Boot 
     * structured logging properties. These properties are set with very high
     * precedence to ensure they cannot be overridden by application properties.
     */
    private void enforceEcsLogging(ConfigurableEnvironment environment) {
        Map<String, Object> ecsProperties = createEcsLoggingProperties(environment);
        
        // Create property source with very high precedence to ensure it cannot be overridden
        MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, ecsProperties) {
            @Override
            public String toString() {
                return "QuickPay ECS Logging Enforcer [" + ecsProperties.keySet() + "]";
            }
        };
        
        // Add with highest precedence to ensure these cannot be overridden
        environment.getPropertySources().addFirst(propertySource);
        
        logger.info("Enforced ECS logging format for QuickPay application. " +
                   "Console and file logging will use ECS format (non-overrideable).");
        logger.debug("Enforced ECS logging properties: {}", ecsProperties.keySet());
    }
    
    /**
     * Creates the non-negotiable ECS logging properties.
     * 
     * These properties are LOCKED and will override any user configuration:
     * - ECS format for console: FORCED
     * - ECS format for file: FORCED  
     * - ECS version: LOCKED to 8.11
     * - Service metadata: Configurable but ECS-compliant
     */
    private Map<String, Object> createEcsLoggingProperties(ConfigurableEnvironment environment) {
        Map<String, Object> props = new HashMap<>();

        props.put("logging.structured.format.console", "ecs");
        props.put("logging.structured.format.file", "ecs");

        String serviceName = environment.getProperty(QUICKPAY_LOGGING_PREFIX + ".service.name", "quickpay-service");
        String serviceVersion = environment.getProperty(QUICKPAY_LOGGING_PREFIX + ".service.version", "1.0.0");
        String serviceEnvironment = environment.getProperty(QUICKPAY_LOGGING_PREFIX + ".service.environment", "development");

        props.put("logging.structured.service.name", serviceName);
        props.put("logging.structured.service.version", serviceVersion);
        props.put("logging.structured.service.environment", serviceEnvironment);

        props.put("logging.structured.ecs.version", "8.11");

        return props;
    }
}