package com.quickpay.client;

import com.quickpay.domain.PaymentProvider;
import com.quickpay.domain.PaymentRequest;
import com.quickpay.domain.PaymentResponse;

/**
 * Interface for payment provider clients.
 * Each provider implementation handles provider-specific API integration.
 */
public interface PaymentProviderClient {

    /**
     * Process a payment through this provider
     *
     * @param request the payment request
     * @return the payment response
     */
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * Get the provider this client supports
     *
     * @return the payment provider
     */
    PaymentProvider getProvider();

    /**
     * Check if this provider is currently available/healthy
     *
     * @return true if the provider is healthy, false otherwise
     */
    default boolean isHealthy() {
        return true;
    }
}
