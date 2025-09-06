package com.quickpay.logging.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment post-processor that configures application-wide ECS structured logging
 * for QuickPay applications. This runs early in the Spring Boot lifecycle to ensure
 * logging configuration is applied before other components initialize.
 */
public class QuickPayLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(QuickPayLoggingEnvironmentPostProcessor.class);

    private static final String QUICK_PAY_ECS_LOGGING_PREFIX = "quickpay.logging.ecs";
    private static final String PROPERTY_SOURCE_NAME = "quickpay-ecs-logging";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isQuickPayLoggingEnabled(environment)) {
            try {
                configureEcsLogging(environment);
                logger.debug("QuickPay ECS logging configuration applied successfully");
            } catch (Exception e) {
                logger.warn("Failed to configure QuickPay ECS logging: {}", e.getMessage());
                logger.debug("QuickPay ECS logging configuration error details", e);
            }
        }
    }

    /**
     * Checks if QuickPay logging is enabled based on configuration properties.
     */
    private boolean isQuickPayLoggingEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty(QUICK_PAY_ECS_LOGGING_PREFIX + ".enabled", Boolean.class, true);
    }

    /**
     * Configures application-wide ECS structured logging by setting the appropriate
     * Spring Boot logging properties.
     */
    private void configureEcsLogging(ConfigurableEnvironment environment) {
        Map<String, Object> ecsProperties = createEcsLoggingProperties(environment);
        
        PropertySource<?> propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, ecsProperties);
        environment.getPropertySources().addFirst(propertySource);
        
        logger.debug("Added ECS logging properties: {}", ecsProperties.keySet());
    }

    /**
     * Creates the map of ECS logging properties based on QuickPay configuration.
     */
    private Map<String, Object> createEcsLoggingProperties(ConfigurableEnvironment environment) {
        Map<String, Object> props = new HashMap<>();

        props.put("logging.structured.format.console", "ecs");
        props.put("logging.structured.format.file", "ecs");

        String serviceName = environment.getProperty(QUICK_PAY_ECS_LOGGING_PREFIX + ".service.name", "quickpay-service");
        String serviceVersion = environment.getProperty(QUICK_PAY_ECS_LOGGING_PREFIX + ".service.version", "unknown");
        String serviceEnvironment = environment.getProperty(QUICK_PAY_ECS_LOGGING_PREFIX + ".service.environment", "development");
        
        props.put("logging.structured.service.name", serviceName);
        props.put("logging.structured.service.version", serviceVersion);
        props.put("logging.structured.service.environment", serviceEnvironment);

        return props;
    }
}