package com.quickpay.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quickpay.domain.PaymentCurrency;
import com.quickpay.domain.PaymentProvider;
import com.quickpay.domain.PaymentRequest;

/**
 * Routes payment requests to appropriate providers based on business rules.
 * Demonstrates using the PaymentProviderRegistry in a realistic way.
 * <p>
 * This component is automatically created by auto-configuration and shows
 * how auto-configured beans collaborate together.
 */
public class PaymentProviderRouter {

    private static final Logger log = LoggerFactory.getLogger(PaymentProviderRouter.class);

    private final PaymentProviderRegistry registry;

    public PaymentProviderRouter(PaymentProviderRegistry registry) {
        this.registry = registry;
        log.info("Initialized PaymentProviderRouter with {} providers",
                registry.getRegisteredProviders().size());
    }

    /**
     * Select the best provider for the given payment request.
     * <p>
     * Routing strategy (simplified example):
     * - USD payments: Prefer Stripe (lower fees for USD)
     * - EUR/GBP payments: Prefer PayPal (good international coverage)
     * - Fallback: Use first available provider
     *
     * @param request the payment request
     * @return selected payment provider client
     * @throws IllegalStateException if no provider is available
     */
    public PaymentProviderClient selectProvider(PaymentRequest request) {
        log.debug("Selecting provider for payment: amount={}, currency={}",
                request.amount(), request.currency());

        // Strategy 1: USD -> prefer Stripe
        if (request.currency() == PaymentCurrency.USD) {
            return registry.getClient(PaymentProvider.STRIPE)
                    .or(() -> registry.getClient(PaymentProvider.PAYPAL))
                    .orElseThrow(() -> new IllegalStateException("No provider available for USD payment"));
        }

        // Strategy 2: EUR/GBP -> prefer PayPal
        if (request.currency() == PaymentCurrency.EUR ||
            request.currency() == PaymentCurrency.GBP) {
            return registry.getClient(PaymentProvider.PAYPAL)
                    .or(() -> registry.getClient(PaymentProvider.STRIPE))
                    .orElseThrow(() -> new IllegalStateException(
                            "No provider available for " + request.currency() + " payment"));
        }

        // Strategy 3: Fallback to first available provider
        return registry.getRegisteredProviders().stream()
                .findFirst()
                .flatMap(registry::getClient)
                .orElseThrow(() -> new IllegalStateException("No payment provider available"));
    }

    /**
     * Select a specific provider by name.
     *
     * @param provider the desired provider
     * @return the provider client
     * @throws IllegalStateException if provider is not available
     */
    public PaymentProviderClient selectProvider(PaymentProvider provider) {
        return registry.getClient(provider)
                .orElseThrow(() -> new IllegalStateException(
                        "Provider " + provider + " is not available"));
    }
}
