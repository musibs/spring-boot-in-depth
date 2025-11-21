package com.quickpay.logging.system;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.json.JsonWriter.Members;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;
import org.springframework.core.env.Environment;

/**
 * JSON members customizer that provides PII (Personally Identifiable Information) masking
 * for structured logging output.
 * <p>
 * This customizer automatically masks sensitive fields in log messages to prevent
 * accidental exposure of PII data. It includes a set of default sensitive field names
 * and supports additional custom field names through configuration.
 * <p>
 * The masking behavior can be controlled via the 'quickpay.logging.pii-masking' property,
 * and additional sensitive fields can be specified via 'quickpay.logging.sensitive-fields'.
 *
 * @author Somnath Musib
 * @since 1.0.0
 * @see StructuredLoggingJsonMembersCustomizer
 */
public class QuickPayPiiMaskingJsonMembersCustomizer implements StructuredLoggingJsonMembersCustomizer<Object> {

	/**
	 * Default set of field names that are considered sensitive and should be masked.
	 * Includes common PII field names such as passwords, tokens, credentials, etc.
	 */
	private static final Set<String> DEFAULT_PII_FIELDS = Set.of("password", "token", "secret", "key",
			"credential", "authorization", "card", "account", "ssn", "email", "phone");

	/**
	 * Flag indicating whether PII masking is enabled.
	 */
	private final boolean enableMasking;

	/**
	 * Set of all sensitive field names that should be masked (default + custom).
	 */
	private final Set<String> sensitiveFields;

	/**
	 * Constructs a new PII masking customizer with configuration from the given environment.
	 * <p>
	 * Reads the PII masking enabled flag from 'quickpay.logging.pii-masking' (defaults to true)
	 * and additional sensitive field names from 'quickpay.logging.sensitive-fields'.
	 * The final set of sensitive fields includes both default fields and any custom fields.
	 *
	 * @param environment the Spring environment containing configuration properties
	 */
	public QuickPayPiiMaskingJsonMembersCustomizer(Environment environment) {
		this.enableMasking = environment.getProperty("quickpay.logging.pii-masking", Boolean.class, Boolean.TRUE);

		String[] customFields = environment.getProperty("quickpay.logging.sensitive-fields", String[].class);

		Set<String> merged = new HashSet<>(DEFAULT_PII_FIELDS);
		if (customFields != null) {
			merged.addAll(Arrays.stream(customFields).map(String::toLowerCase).map(String::trim).toList());
		}
		this.sensitiveFields = merged;
	}

	/**
	 * Customizes the JSON members by applying PII masking to sensitive fields.
	 * <p>
	 * If PII masking is enabled, this method applies a value processor that
	 * checks each field name against the configured sensitive fields set
	 * and masks the value if it's considered sensitive.
	 *
	 * @param members the JSON members to customize
	 */
	@Override
	public void customize(Members<Object> members) {
		if (enableMasking) {
			members.applyingValueProcessor((path, value) -> {
				String fieldName = path.name();
				if (isSensitive(fieldName)) {
					return mask(String.valueOf(value));
				}
				return value;
			});
		}
	}

	/**
	 * Determines if a field name is considered sensitive and should be masked.
	 * <p>
	 * Performs case-insensitive comparison against the configured set of sensitive fields.
	 *
	 * @param fieldName the field name to check
	 * @return true if the field is sensitive and should be masked, false otherwise
	 */
	private boolean isSensitive(String fieldName) {
		return fieldName != null && sensitiveFields.contains(fieldName.toLowerCase());
	}

	/**
	 * Masks a sensitive value by replacing characters with asterisks.
	 * <p>
	 * For values with 4 or fewer characters, returns "***".
	 * For longer values, shows the first 2 and last 2 characters with asterisks in between.
	 * Example: "password123" becomes "pa*******23"
	 *
	 * @param value the value to mask
	 * @return the masked value
	 */
	private String mask(String value) {
		if (value == null || value.length() <= 4)
			return "***";
		return value.substring(0, 2) + "*".repeat(value.length() - 4) + value.substring(value.length() - 2);
	}
}
