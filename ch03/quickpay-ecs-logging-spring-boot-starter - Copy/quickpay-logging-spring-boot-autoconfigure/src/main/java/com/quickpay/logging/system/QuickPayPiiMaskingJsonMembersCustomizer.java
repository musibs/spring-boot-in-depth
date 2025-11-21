package com.quickpay.logging.system;

import org.springframework.boot.json.JsonWriter.Members;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class QuickPayPiiMaskingJsonMembersCustomizer implements StructuredLoggingJsonMembersCustomizer<Object> {

	private static final Set<String> DEFAULT_PII_FIELDS = Set.of("password", "token", "secret", "key",
			"credential", "authorization", "card", "account", "ssn", "email", "phone");

	private final boolean enableMasking;
	private final Set<String> sensitiveFields;

	public QuickPayPiiMaskingJsonMembersCustomizer(Environment environment) {
		this.enableMasking = environment.getProperty("quickpay.logging.pii-masking", Boolean.class, Boolean.TRUE);

		String[] customFields = environment.getProperty("quickpay.logging.sensitive-fields", String[].class);

		Set<String> merged = new HashSet<>(DEFAULT_PII_FIELDS);
		if (customFields != null) {
			merged.addAll(Arrays.stream(customFields).map(String::toLowerCase).map(String::trim).toList());
		}
		this.sensitiveFields = merged;
	}

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

	private boolean isSensitive(String fieldName) {
		return fieldName != null && sensitiveFields.contains(fieldName.toLowerCase());
	}

	private String mask(String value) {
		if (value == null || value.length() <= 4)
			return "***";
		return value.substring(0, 2) + "*".repeat(value.length() - 4) + value.substring(value.length() - 2);
	}
}
