package com.quickpay.autoconfigure;

import com.quickpay.client.StripeClient;
import com.quickpay.config.ProviderConfig;
import com.quickpay.config.QuickPayProviderProperties;
import com.quickpay.config.StripeSecurityConfiguration;
import com.quickpay.domain.PaymentProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestClient;

/**
 * Auto-configuration for Stripe payment provider integration.
 * <p>
 * Demonstrates:
 * <ul>
 *   <li><b>@AutoConfiguration(before)</b> - Ensures Stripe client is created before the registry</li>
 *   <li><b>@ConditionalOnProvider</b> - Custom conditional annotation for provider enablement</li>
 *   <li><b>@Import</b> - Imports provider-specific security configuration</li>
 *   <li><b>@Order</b> - Controls bean priority (Stripe = 1, higher priority)</li>
 * </ul>
 * <p>
 * Configuration properties required:
 * <pre>
 * quickpay.providers.stripe.enabled=true
 * quickpay.providers.stripe.api-key=sk_test_...
 * </pre>
 *
 * @see PaymentProviderAutoConfiguration
 * @see StripeSecurityConfiguration
 */
@AutoConfiguration(before = PaymentProviderAutoConfiguration.class)
@ConditionalOnProvider(PaymentProvider.STRIPE)
@Import(StripeSecurityConfiguration.class)
public class StripeAutoConfiguration {

    /**
     * Creates the StripeClient for processing payments through Stripe.
     * <p>
     * @Order(1) gives Stripe higher priority than other providers.
     * Demonstrates wiring authentication handler into the client.
     *
     * @param properties QuickPay provider properties
     * @param restClient RestClient for HTTP communication
     * @param authHandler Stripe authentication handler (auto-configured via @Import)
     * @return configured StripeClient
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(1)  // Higher priority - Stripe is often the primary provider
    StripeClient stripeClient(
            QuickPayProviderProperties properties,
            RestClient restClient,
            StripeSecurityConfiguration.StripeAuthenticationHandler authHandler) {

        var stripeProps = properties.stripe();

        ProviderConfig config = new ProviderConfig(
                stripeProps.baseUrl(),
                stripeProps.apiKey(),
                properties.common().timeout(),
                properties.common().retryAttempts()
        );

        return new StripeClient(config, restClient, authHandler);
    }
}
