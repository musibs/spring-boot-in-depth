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
 * Environment post-processor that enforces structured logging for all QuickPay applications.
 * <p>
 * This initializer automatically configures ECS (Elastic Common Schema) structured logging
 * by setting Spring Boot structured logging properties with high precedence. It ensures
 * consistent logging format across all QuickPay applications and cannot be overridden
 * by application-specific configuration.
 * <p>
 * The initializer is automatically registered via META-INF/spring.factories and runs
 * during Spring Boot application startup.
 *
 * @author Somnath Musib
 * @since 1.0.0
 * @see EnvironmentPostProcessor
 */
public class QuickPayLoggingSystemInitializer implements EnvironmentPostProcessor {

	/**
	 * Creates a new instance of QuickPayLoggingSystemInitializer.
	 * <p>
	 * This constructor is called automatically by Spring Boot during application startup
	 * as part of the EnvironmentPostProcessor mechanism.
	 */
	public QuickPayLoggingSystemInitializer() {
		// Default constructor for Spring Boot EnvironmentPostProcessor
	}

	/**
	 * Logger for this class.
	 */
	//private static final Logger logger = LoggerFactory.getLogger(QuickPayLoggingSystemInitializer.class);

	/**
	 * Configuration property prefix for QuickPay logging settings.
	 */
	private static final String QUICKPAY_LOGGING_PREFIX = "quickpay.logging";

	/**
	 * Name of the property source created by this initializer.
	 */
	private static final String PROPERTY_SOURCE_NAME = "quickpay-logging-enforcer";

	/**
	 * ECS structured logging format identifier.
	 */
	private static final String STRUCTURED_LOG_FORMAT_ECS = "ecs";

	/**
	 * Fully qualified class name of the PII masking JSON customizer.
	 */
	private static final String STRUCTURED_LOG_CUSTOMIZER = "com.quickpay.logging.system.QuickPayPiiMaskingJsonMembersCustomizer";

	/**
	 * Post-processes the environment to apply QuickPay structured logging configuration.
	 * <p>
	 * This method is called during Spring Boot application startup and checks if QuickPay
	 * logging is enabled. If enabled, it applies the structured logging configuration
	 * with high precedence to ensure consistency across applications.
	 *
	 * @param environment the configurable environment to post-process
	 * @param application the Spring application being started
	 */
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		if (isQuickPayLoggingEnabled(environment)) {
			applyQuickPayLogging(environment);
		} 
	}

	/**
	 * Checks if QuickPay logging is enabled based on configuration properties.
	 * <p>
	 * Reads the 'quickpay.logging.enabled' property from the environment.
	 * If not specified, defaults to true.
	 *
	 * @param environment the environment to check for the enabled property
	 * @return true if QuickPay logging is enabled, false otherwise
	 */
	private boolean isQuickPayLoggingEnabled(ConfigurableEnvironment environment) {
		return environment.getProperty(QUICKPAY_LOGGING_PREFIX + ".enabled", Boolean.class, true);
	}

	/**
	 * Enforces ECS structured logging by automatically setting Spring Boot
	 * structured logging properties.
	 * <p>
	 * Creates and adds a high-precedence property source to the environment
	 * containing ECS logging configuration. This ensures that structured logging
	 * settings cannot be overridden by application-specific configuration files.
	 *
	 * @param environment the environment to apply logging configuration to
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
	 * Creates the custom ECS logging properties map.
	 * <p>
	 * Configures structured logging format for both console and file output,
	 * sets up the PII masking JSON customizer, and defines service metadata
	 * (name, version, environment) based on configuration properties or defaults.
	 *
	 * @param environment the environment to read service configuration from
	 * @return map of logging properties to be applied
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