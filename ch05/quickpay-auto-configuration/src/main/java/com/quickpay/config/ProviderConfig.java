package com.quickpay.config;

import java.time.Duration;

/**
 * Immutable configuration for a payment provider client.
 * Uses Java Record for type-safe configuration.
 *
 * @param baseUrl        the base URL for the provider API
 * @param apiKey         the API key for authentication (should be encrypted in production)
 * @param timeout        request timeout duration
 * @param retryAttempts  number of retry attempts on failure
 */
public record ProviderConfig(
        String baseUrl,
        String apiKey,
        Duration timeout,
        int retryAttempts
) {

    /**
     * Compact constructor with validation
     */
    public ProviderConfig {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL is required");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key is required");
        }
        if (timeout == null || timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        if (retryAttempts < 0) {
            throw new IllegalArgumentException("Retry attempts must be non-negative");
        }
    }

    /**
     * Factory method for creating configuration with default timeout and retry
     */
    public static ProviderConfig of(String baseUrl, String apiKey) {
        return new ProviderConfig(baseUrl, apiKey, Duration.ofSeconds(30), 3);
    }
}
