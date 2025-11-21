package com.quickpay.client;

import com.quickpay.config.PayPalSecurityConfiguration.PayPalAuthenticationHandler;
import com.quickpay.config.ProviderConfig;
import com.quickpay.domain.PaymentProvider;
import com.quickpay.domain.PaymentRequest;
import com.quickpay.domain.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * PayPal payment provider client implementation.
 * Demonstrates provider-specific integration using RestClient with OAuth authentication.
 */
public class PayPalClient implements PaymentProviderClient {

    private static final Logger log = LoggerFactory.getLogger(PayPalClient.class);

    private final ProviderConfig config;
    private final RestClient restClient;
    private final PayPalAuthenticationHandler authHandler;

    public PayPalClient(ProviderConfig config, RestClient restClient, PayPalAuthenticationHandler authHandler) {
        this.config = config;
        this.restClient = restClient;
        this.authHandler = authHandler;

        // Validate client ID format at startup
        if (!authHandler.isValidClientId(config.apiKey())) {
            log.warn("Invalid PayPal client ID format");
        }

        log.info("Initialized PayPalClient with base URL: {}", config.baseUrl());
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.debug("Processing payment through PayPal: amount={}, currency={}",
                  request.amount(), request.currency());

        // Create Basic Auth header for OAuth token endpoint
        // In production, would first get OAuth token, then use it for API calls
        // String basicAuth = authHandler.createBasicAuthHeader(clientId, clientSecret);

        // Simulated payment processing
        // In production workflow:
        // 1. Get OAuth token: POST /v1/oauth2/token with Basic Auth
        // 2. Use token: POST /v1/payments/payment with Bearer token

        String transactionId = "paypal_" + UUID.randomUUID();
        log.info("Payment processed successfully via PayPal: transactionId={}", transactionId);

        return PaymentResponse.success(transactionId, PaymentProvider.PAYPAL);
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.PAYPAL;
    }

    @Override
    public boolean isHealthy() {
        // In production, this could ping the PayPal API health endpoint
        return true;
    }
}
