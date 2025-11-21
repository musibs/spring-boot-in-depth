package com.quickpay.autoconfigure;

import com.quickpay.client.PaymentProviderRegistry;
import com.quickpay.client.StripeClient;
import com.quickpay.config.QuickPayProviderProperties;
import com.quickpay.config.StripeSecurityConfiguration;
import com.quickpay.domain.PaymentProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StripeAutoConfiguration}.
 * <p>
 * Demonstrates: - Testing provider-specific auto-configuration -
 * Testing @ConditionalOnProvider custom annotation - Testing @Import of
 * provider security configuration - Testing @AutoConfiguration(before=)
 * ordering - Testing @Order annotation for bean priority
 */
class StripeAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
			AutoConfigurations.of(StripeAutoConfiguration.class, PaymentProviderAutoConfiguration.class));

	@Test
	void stripeClientShouldBeCreatedWhenEnabled() {
		this.contextRunner.withPropertyValues("quickpay.providers.stripe.enabled=true",
				"quickpay.providers.stripe.api-key=sk_test_12345",
				"quickpay.providers.stripe.base-url=https://api.stripe.com").run(context -> {
					assertThat(context).hasSingleBean(StripeClient.class);

					StripeClient client = context.getBean(StripeClient.class);
					assertThat(client.getProvider()).isEqualTo(PaymentProvider.STRIPE);
					assertThat(client.isHealthy()).isTrue();
				});
	}

	@Test
	void stripeClientShouldNotBeCreatedWhenDisabled() {
		this.contextRunner.withPropertyValues("quickpay.providers.stripe.enabled=false").run(context -> {
			assertThat(context).doesNotHaveBean(StripeClient.class);
		});
	}

	@Test
	void stripeClientShouldNotBeCreatedWhenPropertyMissing() {
		this.contextRunner.run(context -> {
			// matchIfMissing = false in @ConditionalOnProvider
			assertThat(context).doesNotHaveBean(StripeClient.class);
		});
	}

	@Test
	void stripeSecurityConfigurationShouldBeImported() {
		this.contextRunner.withPropertyValues("quickpay.providers.stripe.enabled=true",
				"quickpay.providers.stripe.api-key=sk_test_12345").run(context -> {
					assertThat(context).hasSingleBean(StripeSecurityConfiguration.StripeAuthenticationHandler.class);
				});
	}

	@Test
	void stripeClientShouldBeRegisteredInRegistry() {
		this.contextRunner.withPropertyValues("quickpay.providers.stripe.enabled=true",
				"quickpay.providers.stripe.api-key=sk_test_12345").run(context -> {
					assertThat(context).hasSingleBean(PaymentProviderRegistry.class);

					PaymentProviderRegistry registry = context.getBean(PaymentProviderRegistry.class);
					assertThat(registry.getRegisteredProviders()).contains(PaymentProvider.STRIPE);
					assertThat(registry.isProviderRegistered(PaymentProvider.STRIPE)).isTrue();
					assertThat(registry.getClient(PaymentProvider.STRIPE)).isPresent();
				});
	}

	@Test
	void stripeClientShouldBeCreatedBeforeRegistry() {
		this.contextRunner.withPropertyValues("quickpay.providers.stripe.enabled=true",
				"quickpay.providers.stripe.api-key=sk_test_12345").run(context -> {
					// @AutoConfiguration(before = PaymentProviderAutoConfiguration.class)
					assertThat(context).hasSingleBean(StripeClient.class);
					assertThat(context).hasSingleBean(PaymentProviderRegistry.class);

					PaymentProviderRegistry registry = context.getBean(PaymentProviderRegistry.class);
					assertThat(registry.getRegisteredProviders()).hasSize(1);
				});
	}

	@Test
	void configurationPropertiesShouldBindCorrectly() {
		this.contextRunner
				.withPropertyValues("quickpay.providers.stripe.enabled=true",
						"quickpay.providers.stripe.api-key=sk_test_custom_key",
						"quickpay.providers.stripe.base-url=https://custom.stripe.com",
						"quickpay.providers.common.timeout=45s", "quickpay.providers.common.retry-attempts=5")
				.run(context -> {
					assertThat(context).hasSingleBean(QuickPayProviderProperties.class);

					QuickPayProviderProperties props = context.getBean(QuickPayProviderProperties.class);
					assertThat(props.stripe().enabled()).isTrue();
					assertThat(props.stripe().apiKey()).isEqualTo("sk_test_custom_key");
					assertThat(props.stripe().baseUrl()).isEqualTo("https://custom.stripe.com");
					assertThat(props.common().timeout()).hasSeconds(45);
					assertThat(props.common().retryAttempts()).isEqualTo(5);
				});
	}
}
