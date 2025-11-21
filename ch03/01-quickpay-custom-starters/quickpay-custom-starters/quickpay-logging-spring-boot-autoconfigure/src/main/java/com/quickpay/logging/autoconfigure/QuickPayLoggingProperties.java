package com.quickpay.logging.autoconfigure;

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

    /**
     * Whether QuickPay logging is enabled.
     * Default is true.
     */
    private boolean enabled = true;

    /**
     * ECS (Elastic Common Schema) configuration for structured logging.
     */
    @Valid
    private EcsConfig ecs = new EcsConfig();

    /**
     * Transaction correlation configuration for tracking requests across services.
     */
    @Valid
    private CorrelationConfig correlation = new CorrelationConfig();

    /**
     * Service identification configuration.
     */
    @Valid
    private ServiceConfig service = new ServiceConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public EcsConfig getEcs() {
        return ecs;
    }

    public void setEcs(EcsConfig ecs) {
        this.ecs = ecs;
    }

    public CorrelationConfig getCorrelation() {
        return correlation;
    }

    public void setCorrelation(CorrelationConfig correlation) {
        this.correlation = correlation;
    }

    public ServiceConfig getService() {
        return service;
    }

    public void setService(ServiceConfig service) {
        this.service = service;
    }

    /**
     * ECS configuration for structured logging.
     */
    public static class EcsConfig {
        private boolean piiMasking = true;

        public boolean isPiiMasking() {
            return piiMasking;
        }

        public void setPiiMasking(boolean piiMasking) {
            this.piiMasking = piiMasking;
        }
    }

    /**
     * Transaction correlation configuration.
     */
    public static class CorrelationConfig {
        private boolean enabled = true;

        @NotBlank
        private String headerName = "X-Transaction-ID";

        private boolean generateIfMissing = true;
        private boolean addToResponse = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            if (headerName != null && headerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Header name cannot be empty");
            }
            this.headerName = headerName;
        }

        public boolean isGenerateIfMissing() {
            return generateIfMissing;
        }

        public void setGenerateIfMissing(boolean generateIfMissing) {
            this.generateIfMissing = generateIfMissing;
        }

        public boolean isAddToResponse() {
            return addToResponse;
        }

        public void setAddToResponse(boolean addToResponse) {
            this.addToResponse = addToResponse;
        }
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