package com.quickpay.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe-specific configuration imported by StripeAutoConfiguration.
 * <p>
 * Demonstrates:
 * - Provider-specific configuration via @Import
 * - Encapsulating provider authentication logic
 */
@Configuration(proxyBeanMethods = false)
public class StripeSecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    StripeAuthenticationHandler stripeAuthenticationHandler() {
        return new StripeAuthenticationHandler();
    }

    /**
     * Simple authentication handler for Stripe API.
     * In production, this would handle API key validation, request signing, etc.
     */
    public static class StripeAuthenticationHandler {

        /**
         * Add Stripe authentication headers to a request
         *
         * @param apiKey the Stripe API key
         * @return authorization header value
         */
        public String createAuthorizationHeader(String apiKey) {
            // Stripe uses Bearer token authentication
            return "Bearer " + apiKey;
        }

        /**
         * Validate Stripe API key format
         *
         * @param apiKey the API key to validate
         * @return true if valid format
         */
        public boolean isValidApiKey(String apiKey) {
            return apiKey != null && (apiKey.startsWith("sk_") || apiKey.startsWith("pk_"));
        }
    }
}
