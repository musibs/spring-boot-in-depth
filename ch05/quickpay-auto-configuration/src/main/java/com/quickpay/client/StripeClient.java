package com.quickpay.client;

import com.quickpay.config.ProviderConfig;
import com.quickpay.config.StripeSecurityConfiguration.StripeAuthenticationHandler;
import com.quickpay.domain.PaymentProvider;
import com.quickpay.domain.PaymentRequest;
import com.quickpay.domain.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Stripe payment provider client implementation.
 * Demonstrates provider-specific integration using RestClient with authentication.
 */
public class StripeClient implements PaymentProviderClient {

    private static final Logger log = LoggerFactory.getLogger(StripeClient.class);

    private final ProviderConfig config;
    private final RestClient restClient;
    private final StripeAuthenticationHandler authHandler;

    public StripeClient(ProviderConfig config, RestClient restClient, StripeAuthenticationHandler authHandler) {
        this.config = config;
        this.restClient = restClient;
        this.authHandler = authHandler;

        // Validate API key format at startup
        if (!authHandler.isValidApiKey(config.apiKey())) {
            log.warn("Invalid Stripe API key format. Expected format: sk_* or pk_*");
        }

        log.info("Initialized StripeClient with base URL: {}", config.baseUrl());
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.debug("Processing payment through Stripe: amount={}, currency={}",
                  request.amount(), request.currency());

        // Create authorization header using authentication handler
        String authHeader = authHandler.createAuthorizationHeader(config.apiKey());

        // Simulated payment processing
        // In production, this would use RestClient with the auth header:
        // restClient.post()
        //     .uri(config.baseUrl() + "/v1/charges")
        //     .header("Authorization", authHeader)
        //     .body(...)
        //     .retrieve()

        String transactionId = "stripe_" + UUID.randomUUID();
        log.info("Payment processed successfully via Stripe: transactionId={}", transactionId);

        return PaymentResponse.success(transactionId, PaymentProvider.STRIPE);
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}
