package com.quickpay.autoconfigure;

import com.quickpay.client.PayPalClient;
import com.quickpay.client.PaymentProviderRegistry;
import com.quickpay.config.PayPalSecurityConfiguration;
import com.quickpay.domain.PaymentProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PayPalAutoConfiguration}.
 * <p>
 * Demonstrates:
 * - Testing PayPal-specific auto-configuration
 * - Testing @ConditionalOnProvider with different provider
 * - Testing @Import of PayPal security configuration
 * - Testing multiple providers together
 */
class PayPalAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    PayPalAutoConfiguration.class,
                    PaymentProviderAutoConfiguration.class
            ));

    @Test
    void paypalClientShouldBeCreatedWhenEnabled() {
        this.contextRunner
                .withPropertyValues(
                        "quickpay.providers.paypal.enabled=true",
                        "quickpay.providers.paypal.client-id=test_client_id_12345",
                        "quickpay.providers.paypal.client-secret=test_secret",
                        "quickpay.providers.paypal.base-url=https://api.paypal.com",
                        "quickpay.providers.paypal.mode=sandbox"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PayPalClient.class);

                    PayPalClient client = context.getBean(PayPalClient.class);
                    assertThat(client.getProvider()).isEqualTo(PaymentProvider.PAYPAL);
                    assertThat(client.isHealthy()).isTrue();
                });
    }

    @Test
    void paypalClientShouldNotBeCreatedWhenDisabled() {
        this.contextRunner
                .withPropertyValues("quickpay.providers.paypal.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PayPalClient.class);
                });
    }

    @Test
    void paypalSecurityConfigurationShouldBeImported() {
        this.contextRunner
                .withPropertyValues(
                        "quickpay.providers.paypal.enabled=true",
                        "quickpay.providers.paypal.client-id=test_client_id",
                        "quickpay.providers.paypal.client-secret=test_secret"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(
                            PayPalSecurityConfiguration.PayPalAuthenticationHandler.class);
                });
    }

    @Test
    void paypalClientShouldBeRegisteredInRegistry() {
        this.contextRunner
                .withPropertyValues(
                        "quickpay.providers.paypal.enabled=true",
                        "quickpay.providers.paypal.client-id=test_client_id",
                        "quickpay.providers.paypal.client-secret=test_secret"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PaymentProviderRegistry.class);

                    PaymentProviderRegistry registry = context.getBean(PaymentProviderRegistry.class);
                    assertThat(registry.getRegisteredProviders()).contains(PaymentProvider.PAYPAL);
                    assertThat(registry.isProviderRegistered(PaymentProvider.PAYPAL)).isTrue();
                    assertThat(registry.getClient(PaymentProvider.PAYPAL)).isPresent();
                });
    }

    @Test
    void multipleProvidersShouldCoexist() {
        this.contextRunner
                .withConfiguration(AutoConfigurations.of(StripeAutoConfiguration.class))
                .withPropertyValues(
                        "quickpay.providers.stripe.enabled=true",
                        "quickpay.providers.stripe.api-key=sk_test_12345",
                        "quickpay.providers.paypal.enabled=true",
                        "quickpay.providers.paypal.client-id=test_client_id",
                        "quickpay.providers.paypal.client-secret=test_secret"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PayPalClient.class);
                    assertThat(context).hasBean("stripeClient");

                    PaymentProviderRegistry registry = context.getBean(PaymentProviderRegistry.class);
                    assertThat(registry.getRegisteredProviders())
                            .hasSize(2)
                            .contains(PaymentProvider.STRIPE, PaymentProvider.PAYPAL);
                });
    }

    @Test
    void providerOrderingShouldBeRespected() {
        this.contextRunner
                .withConfiguration(AutoConfigurations.of(StripeAutoConfiguration.class))
                .withPropertyValues(
                        "quickpay.providers.stripe.enabled=true",
                        "quickpay.providers.stripe.api-key=sk_test_12345",
                        "quickpay.providers.paypal.enabled=true",
                        "quickpay.providers.paypal.client-id=test_client_id",
                        "quickpay.providers.paypal.client-secret=test_secret"
                )
                .run(context -> {
                    // Stripe has @Order(1), PayPal has @Order(2)
                    PaymentProviderRegistry registry = context.getBean(PaymentProviderRegistry.class);
                    assertThat(registry.getRegisteredProviders().get(0)).isEqualTo(PaymentProvider.STRIPE);
                    assertThat(registry.getRegisteredProviders().get(1)).isEqualTo(PaymentProvider.PAYPAL);
                });
    }

    @Test
    void paypalModeShouldBeConfigurable() {
        this.contextRunner
                .withPropertyValues(
                        "quickpay.providers.paypal.enabled=true",
                        "quickpay.providers.paypal.client-id=test_client_id",
                        "quickpay.providers.paypal.client-secret=test_secret",
                        "quickpay.providers.paypal.mode=live"
                )
                .run(context -> {
                    var props = context.getBean(com.quickpay.config.QuickPayProviderProperties.class);
                    assertThat(props.paypal().mode()).isEqualTo("live");
                });
    }
}
