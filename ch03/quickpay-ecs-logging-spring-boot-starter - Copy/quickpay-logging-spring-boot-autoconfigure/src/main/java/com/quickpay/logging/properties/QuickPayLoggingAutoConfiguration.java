package com.quickpay.logging.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.logging.structured.StructuredLogFormatter;
import org.springframework.context.annotation.Bean;

import com.quickpay.logging.formatter.QuickPayMaskingLogFormatter;

/**
 * Auto-configuration for QuickPay masking logging functionality.
 */
@AutoConfiguration
@ConditionalOnClass({ StructuredLogFormatter.class })
@ConditionalOnProperty(prefix = "quickpay.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(QuickPayLoggingProperties.class)
public class QuickPayLoggingAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(QuickPayLoggingAutoConfiguration.class);

	/*    *//**
			 * Creates the QuickPay structured log formatter.
			 *
			 * @param properties the QuickPay logging configuration properties
			 * @return a QuickPayMaskingLogFormatter instance configured with service
			 *         metadata
			 *//*
				 * @Bean
				 * 
				 * @ConditionalOnMissingBean(name = "quickPayMaskingLogFormatter")
				 * QuickPayMaskingLogFormatter
				 * quickPayMaskingLogFormatter(QuickPayLoggingProperties properties) {
				 * logger.info("Creating QuickPay Masking log formatter for service: {}",
				 * properties.getService().getName());
				 * 
				 * return new QuickPayMaskingLogFormatter( properties.isPiiMasking() ); }
				 */
}