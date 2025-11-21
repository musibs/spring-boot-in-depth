package com.quickpay.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * PayPal-specific configuration imported by PayPalAutoConfiguration.
 * <p>
 * Demonstrates:
 * - Provider-specific configuration via @Import
 * - Encapsulating provider OAuth logic
 */
@Configuration(proxyBeanMethods = false)
public class PayPalSecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    PayPalAuthenticationHandler paypalAuthenticationHandler() {
        return new PayPalAuthenticationHandler();
    }

    /**
     * Simple authentication handler for PayPal API.
     * In production, this would handle OAuth token exchange, refresh, etc.
     */
    public static class PayPalAuthenticationHandler {

        /**
         * Create Basic Authentication header for PayPal OAuth
         *
         * @param clientId     the PayPal client ID
         * @param clientSecret the PayPal client secret
         * @return authorization header value
         */
        public String createBasicAuthHeader(String clientId, String clientSecret) {
            // PayPal uses Basic Auth for OAuth token endpoint
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            return "Basic " + encodedCredentials;
        }

        /**
         * Validate PayPal client credentials format
         *
         * @param clientId the client ID to validate
         * @return true if valid format
         */
        public boolean isValidClientId(String clientId) {
            // PayPal client IDs are typically long alphanumeric strings
            return clientId != null && clientId.length() > 20;
        }
    }
}
