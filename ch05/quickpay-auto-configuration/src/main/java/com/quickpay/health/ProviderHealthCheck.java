package com.quickpay.health;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quickpay.client.PaymentProviderRegistry;
import com.quickpay.domain.PaymentProvider;

/**
 * Health check component for payment providers.
 * Monitors the health status of all registered payment providers.
 * <p>
 * Demonstrates:
 * - Working with optional dependencies (registry can be null)
 * - Graceful degradation when dependencies are missing
 * - Simple health monitoring pattern
 */
public class ProviderHealthCheck {

    private static final Logger log = LoggerFactory.getLogger(ProviderHealthCheck.class);

    private final PaymentProviderRegistry registry;

    /**
     * Constructor accepting optional PaymentProviderRegistry.
     * The registry can be null if no providers are configured.
     *
     * @param registry the payment provider registry (can be null)
     */
    public ProviderHealthCheck(@Nullable PaymentProviderRegistry registry) {
        this.registry = registry;
        log.info("ProviderHealthCheck initialized with registry: {}", registry != null ? "present" : "absent");
    }

    /**
     * Check health of all registered providers.
     * Returns a map of provider to health status.
     *
     * @return map of provider health status
     */
    public Map<String, HealthStatus> checkHealth() {
        Map<String, HealthStatus> healthMap = new HashMap<>();

        if (registry == null) {
            log.debug("No registry available - no providers to check");
            return healthMap;
        }

        List<PaymentProvider> healthyProviders = registry.getHealthyProviders();
        List<PaymentProvider> allProviders = registry.getRegisteredProviders();

        for (PaymentProvider provider : allProviders) {
            boolean isHealthy = healthyProviders.contains(provider);
            healthMap.put(provider.name(), new HealthStatus(isHealthy, isHealthy ? "OK" : "UNHEALTHY"));
        }

        log.debug("Health check completed: {}/{} providers healthy",
                  healthyProviders.size(), allProviders.size());

        return healthMap;
    }

    /**
     * Check if all providers are healthy.
     *
     * @return true if all providers are healthy, false otherwise
     */
    public boolean areAllProvidersHealthy() {
        if (registry == null) {
            return true; // No providers means nothing is unhealthy
        }

        int healthy = registry.getHealthyProviders().size();
        int total = registry.getRegisteredProviders().size();

        return healthy == total;
    }

    /**
     * Get count of healthy providers.
     *
     * @return number of healthy providers
     */
    public int getHealthyProviderCount() {
        return registry != null ? registry.getHealthyProviders().size() : 0;
    }

    /**
     * Get count of total registered providers.
     *
     * @return number of registered providers
     */
    public int getTotalProviderCount() {
        return registry != null ? registry.getRegisteredProviders().size() : 0;
    }

    /**
     * Health status record for a provider.
     *
     * @param healthy true if provider is healthy
     * @param message status message
     */
    public record HealthStatus(boolean healthy, String message) {
    }
}
