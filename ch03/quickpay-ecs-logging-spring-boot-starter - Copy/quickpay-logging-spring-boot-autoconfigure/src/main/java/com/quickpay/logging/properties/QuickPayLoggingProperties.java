package com.quickpay.logging.properties;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Configuration properties for QuickPay logging functionality.
 *
 * @author Somnath Musib
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "quickpay.logging")
@Validated
public class QuickPayLoggingProperties {

	private boolean enabled = true;

	private boolean piiMasking = true;

	private String[] sensitiveFields;

	@Valid
	private ServiceConfig service = new ServiceConfig();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isPiiMasking() {
		return piiMasking;
	}

	public void setPiiMasking(boolean piiMasking) {
		this.piiMasking = piiMasking;
	}

	public String[] getSensitiveFields() {
		return sensitiveFields;
	}

	public void setSensitiveFields(String[] sensitiveFields) {
		this.sensitiveFields = sensitiveFields;
	}

	public ServiceConfig getService() {
		return service;
	}

	public void setService(ServiceConfig service) {
		this.service = service;
	}

	/**
	 * Service identification configuration.
	 */
	public static class ServiceConfig {
		@NotBlank
		private String name = "quickpay-service";

		@NotBlank
		private String version = "1.0.0";

		@NotBlank
		private String environment = "development";

		private Map<String, String> metadata = Map.of();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			if (name != null && name.trim().isEmpty()) {
				throw new IllegalArgumentException("Service name cannot be empty");
			}
			this.name = name;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			if (version != null && version.trim().isEmpty()) {
				throw new IllegalArgumentException("Service version cannot be empty");
			}
			this.version = version;
		}

		public String getEnvironment() {
			return environment;
		}

		public void setEnvironment(String environment) {
			if (environment != null && environment.trim().isEmpty()) {
				throw new IllegalArgumentException("Service environment cannot be empty");
			}
			this.environment = environment;
		}

		public Map<String, String> getMetadata() {
			return metadata;
		}

		public void setMetadata(Map<String, String> metadata) {
			this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
		}
	}
}