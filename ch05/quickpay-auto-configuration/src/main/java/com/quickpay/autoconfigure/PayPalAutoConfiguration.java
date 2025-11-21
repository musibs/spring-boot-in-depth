package com.quickpay.autoconfigure;

import com.quickpay.client.PayPalClient;
import com.quickpay.config.PayPalSecurityConfiguration;
import com.quickpay.config.ProviderConfig;
import com.quickpay.config.QuickPayProviderProperties;
import com.quickpay.domain.PaymentProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestClient;

/**
 * Auto-configuration for PayPal payment provider integration.
 * <p>
 * Demonstrates:
 * <ul>
 *   <li><b>@AutoConfiguration(before)</b> - Ensures PayPal client is created before the registry</li>
 *   <li><b>@ConditionalOnProvider</b> - Custom conditional annotation for provider enablement</li>
 *   <li><b>@Import</b> - Imports provider-specific security configuration</li>
 *   <li><b>@Order</b> - Controls bean priority (PayPal = 2, lower than Stripe)</li>
 * </ul>
 * <p>
 * Configuration properties required:
 * <pre>
 * quickpay.providers.paypal.enabled=true
 * quickpay.providers.paypal.client-id=...
 * quickpay.providers.paypal.client-secret=...
 * </pre>
 *
 * @see PaymentProviderAutoConfiguration
 * @see PayPalSecurityConfiguration
 */
@AutoConfiguration(before = PaymentProviderAutoConfiguration.class)
@ConditionalOnProvider(PaymentProvider.PAYPAL)
@Import(PayPalSecurityConfiguration.class)
public class PayPalAutoConfiguration {

    /**
     * Creates the PayPalClient for processing payments through PayPal.
     * <p>
     * @Order(2) gives PayPal lower priority than Stripe.
     * Demonstrates wiring authentication handler into the client.
     *
     * @param properties QuickPay provider properties
     * @param restClient RestClient for HTTP communication
     * @param authHandler PayPal authentication handler (auto-configured via @Import)
     * @return configured PayPalClient
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(2)  // Lower priority than Stripe
    PayPalClient paypalClient(
            QuickPayProviderProperties properties,
            RestClient restClient,
            PayPalSecurityConfiguration.PayPalAuthenticationHandler authHandler) {

        var paypalProps = properties.paypal();

        ProviderConfig config = new ProviderConfig(
                paypalProps.baseUrl(),
                paypalProps.clientId(),
                properties.common().timeout(),
                properties.common().retryAttempts()
        );

        return new PayPalClient(config, restClient, authHandler);
    }
}
