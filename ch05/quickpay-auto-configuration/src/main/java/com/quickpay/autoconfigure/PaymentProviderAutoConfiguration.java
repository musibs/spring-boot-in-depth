package com.quickpay.autoconfigure;

import com.quickpay.client.PaymentProviderClient;
import com.quickpay.client.PaymentProviderRegistry;
import com.quickpay.client.PaymentProviderRouter;
import com.quickpay.config.HttpClientConfiguration;
import com.quickpay.config.QuickPayProviderProperties;
import com.quickpay.health.ProviderHealthCheck;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * Core auto-configuration for QuickPay payment provider integration.
 * <p>
 * Demonstrates key Spring Boot auto-configuration concepts:
 * <ul>
 *   <li><b>@AutoConfiguration</b> - Modern auto-configuration annotation</li>
 *   <li><b>@Import</b> - Externalizing configuration concerns</li>
 *   <li><b>@EnableConfigurationProperties</b> - Type-safe configuration with Records</li>
 *   <li><b>ObjectProvider</b> - Handling optional dependencies and multiple beans</li>
 *   <li><b>@ConditionalOnProperty</b> - Property-based conditional bean creation</li>
 * </ul>
 * <p>
 * This auto-configuration imports {@link HttpClientConfiguration} for RestClient setup.
 * Provider-specific configurations (Stripe, PayPal) are processed before this using
 * {@code @AutoConfiguration(before = PaymentProviderAutoConfiguration.class)}.
 *
 * @see StripeAutoConfiguration
 * @see PayPalAutoConfiguration
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "quickpay.providers",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(QuickPayProviderProperties.class)
@Import(HttpClientConfiguration.class)
public class PaymentProviderAutoConfiguration {

    /**
     * Creates the PaymentProviderRegistry that manages all payment provider clients.
     * <p>
     * Demonstrates <b>ObjectProvider.orderedStream()</b>:
     * <ul>
     *   <li>Collects all PaymentProviderClient beans from the context</li>
     *   <li>Respects @Order annotations for priority</li>
     *   <li>Gracefully handles when no providers are configured</li>
     * </ul>
     *
     * @param providerClients ObjectProvider for all PaymentProviderClient beans
     * @return configured PaymentProviderRegistry
     */
    @Bean
    @ConditionalOnMissingBean
    PaymentProviderRegistry paymentProviderRegistry(
            ObjectProvider<PaymentProviderClient> providerClients) {

        // ObjectProvider.orderedStream() - collect all provider clients respecting @Order
        List<PaymentProviderClient> clients = providerClients.orderedStream().toList();
        return new PaymentProviderRegistry(clients);
    }

    /**
     * Creates the ProviderHealthCheck for monitoring provider health.
     * <p>
     * Demonstrates <b>ObjectProvider.getIfAvailable()</b>:
     * <ul>
     *   <li>Returns the bean if available, null otherwise</li>
     *   <li>Allows health check to work even without a registry</li>
     * </ul>
     *
     * @param registryProvider ObjectProvider for PaymentProviderRegistry
     * @return configured ProviderHealthCheck
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "quickpay.providers", name = "health-check-enabled", matchIfMissing = true)
    ProviderHealthCheck providerHealthCheck(
            ObjectProvider<PaymentProviderRegistry> registryProvider) {

        // ObjectProvider.getIfAvailable() - returns null if bean doesn't exist
        PaymentProviderRegistry registry = registryProvider.getIfAvailable();
        return new ProviderHealthCheck(registry);
    }

    /**
     * Creates the PaymentProviderRouter for intelligent provider selection.
     * <p>
     * Demonstrates how auto-configured beans work together:
     * <ul>
     *   <li>Router uses the auto-configured Registry</li>
     *   <li>Shows realistic usage of the registry pattern</li>
     *   <li>Provides business logic on top of infrastructure</li>
     * </ul>
     *
     * @param registry the payment provider registry
     * @return configured PaymentProviderRouter
     */
    @Bean
    @ConditionalOnMissingBean
    PaymentProviderRouter paymentProviderRouter(PaymentProviderRegistry registry) {
        return new PaymentProviderRouter(registry);
    }
}
