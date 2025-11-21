package com.quickpay.logging.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for QuickPay logging functionality.
 *
 * @author Somnath Musib
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "quickpay.logging")
@Validated
public class QuickPayLoggingProperties {

	/**
	 * Creates a new instance of QuickPayLoggingProperties with default values.
	 * <p>
	 * Default values:
	 * <ul>
	 *   <li>enabled: true</li>
	 *   <li>piiMasking: true</li>
	 *   <li>sensitiveFields: null</li>
	 * </ul>
	 */
	public QuickPayLoggingProperties() {
		// Default constructor with implicit default values
	}

	/**
	 * Whether QuickPay logging is enabled.
	 * Default value is true.
	 */
	private boolean enabled = true;

	/**
	 * Whether PII (Personally Identifiable Information) masking is enabled.
	 * When true, sensitive data will be masked in log outputs.
	 * Default value is true.
	 */
	private boolean piiMasking = true;

	/**
	 * Array of field names that should be treated as sensitive and masked in logs.
	 * These fields will be masked when PII masking is enabled.
	 */
	private String[] sensitiveFields;

	/**
	 * Returns whether QuickPay logging is enabled.
	 *
	 * @return true if logging is enabled, false otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether QuickPay logging is enabled.
	 *
	 * @param enabled true to enable logging, false to disable
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns whether PII masking is enabled.
	 *
	 * @return true if PII masking is enabled, false otherwise
	 */
	public boolean isPiiMasking() {
		return piiMasking;
	}

	/**
	 * Sets whether PII masking is enabled.
	 *
	 * @param piiMasking true to enable PII masking, false to disable
	 */
	public void setPiiMasking(boolean piiMasking) {
		this.piiMasking = piiMasking;
	}

	/**
	 * Returns the array of sensitive field names that should be masked in logs.
	 *
	 * @return array of sensitive field names, or null if not configured
	 */
	public String[] getSensitiveFields() {
		return sensitiveFields;
	}

	/**
	 * Sets the array of sensitive field names that should be masked in logs.
	 *
	 * @param sensitiveFields array of field names to be treated as sensitive
	 */
	public void setSensitiveFields(String[] sensitiveFields) {
		this.sensitiveFields = sensitiveFields;
	}
}