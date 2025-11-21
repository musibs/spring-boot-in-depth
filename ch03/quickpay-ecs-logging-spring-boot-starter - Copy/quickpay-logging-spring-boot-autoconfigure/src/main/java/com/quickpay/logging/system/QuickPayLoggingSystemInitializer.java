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
 * Environment post-processor that enforces structured logging for all QuickPay
 * applications.
 */
public class QuickPayLoggingSystemInitializer implements EnvironmentPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(QuickPayLoggingSystemInitializer.class);

	private static final String QUICKPAY_LOGGING_PREFIX = "quickpay.logging";
	private static final String PROPERTY_SOURCE_NAME = "quickpay-logging-enforcer";
	private static final String STRUCTURED_LOG_FORMAT_ECS = "ecs";
	private static final String STRUCTURED_LOG_CUSTOMIZER = "com.quickpay.logging.system.QuickPayPiiMaskingJsonMembersCustomizer";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		if (isQuickPayLoggingEnabled(environment)) {
			applyQuickPayLogging(environment);
		} else {
			logger.debug("QuickPay logging is disabled");
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
	private void applyQuickPayLogging(ConfigurableEnvironment environment) {
		Map<String, Object> ecsProperties = createLoggingProperties(environment);

		// Create property source with very high precedence to ensure it cannot be
		// overridden
		MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, ecsProperties) {
			@Override
			public String toString() {
				return "QuickPay ECS Logging Enforcer [" + ecsProperties.keySet() + "]";
			}
		};
		environment.getPropertySources().addFirst(propertySource);
	}

	/**
	 * Creates the custom ECS logging properties.
	 */
	private Map<String, Object> createLoggingProperties(ConfigurableEnvironment environment) {
		Map<String, Object> props = new HashMap<>();

		props.put("logging.structured.format.console", STRUCTURED_LOG_FORMAT_ECS);
		props.put("logging.structured.format.file", STRUCTURED_LOG_FORMAT_ECS);
		props.put("logging.structured.json.customizer", STRUCTURED_LOG_CUSTOMIZER);
		String serviceName = environment.getProperty(QUICKPAY_LOGGING_PREFIX + ".service.name", "quickpay-service");
		String serviceVersion = environment.getProperty(QUICKPAY_LOGGING_PREFIX + ".service.version", "1.0.0");
		String serviceEnvironment = environment.getProperty(QUICKPAY_LOGGING_PREFIX + ".service.environment",
				"development");

		props.put("logging.structured.service.name", serviceName);
		props.put("logging.structured.service.version", serviceVersion);
		props.put("logging.structured.service.environment", serviceEnvironment);

		return props;
	}
}