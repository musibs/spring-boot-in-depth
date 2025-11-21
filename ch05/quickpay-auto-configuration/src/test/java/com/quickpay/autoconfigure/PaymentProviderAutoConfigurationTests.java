package com.quickpay.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import com.quickpay.client.PaymentProviderRegistry;
import com.quickpay.health.ProviderHealthCheck;

/**
 * Tests for {@link PaymentProviderAutoConfiguration}.
 * <p>
 * Demonstrates:
 * - Testing auto-configuration with ApplicationContextRunner
 * - Testing conditional beans
 * - Testing @Import functionality
 * - Testing property-based configuration
 * - Testing ObjectProvider usage patterns
 */
class PaymentProviderAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PaymentProviderAutoConfiguration.class));

    @Test
    void autoConfigurationShouldCreateCoreBeansWhenEnabled() {
        this.contextRunner
                .withPropertyValues("quickpay.providers.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(PaymentProviderRegistry.class);
                    assertThat(context).hasSingleBean(ProviderHealthCheck.class);
                });
    }

    @Test
    void autoConfigurationShouldNotCreateBeansWhenDisabled() {
        this.contextRunner
                .withPropertyValues("quickpay.providers.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PaymentProviderRegistry.class);
                    assertThat(context).doesNotHaveBean(ProviderHealthCheck.class);
                });
    }

    @Test
    void autoConfigurationShouldImportHttpClientConfiguration() {
        this.contextRunner
                .withPropertyValues("quickpay.providers.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(RestClient.Builder.class);
                    assertThat(context).hasSingleBean(RestClient.class);
                });
    }

    @Test
    void registryShouldUseObjectProviderOrderedStream() {
        this.contextRunner
                .withPropertyValues("quickpay.providers.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(PaymentProviderRegistry.class);

                    PaymentProviderRegistry registry = context.getBean(PaymentProviderRegistry.class);
                    // With no providers configured, registry should be empty
                    assertThat(registry.getRegisteredProviders()).isEmpty();
                });
    }

    @Test
    void healthCheckShouldUseObjectProviderGetIfAvailable() {
        this.contextRunner
                .withPropertyValues("quickpay.providers.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(ProviderHealthCheck.class);

                    ProviderHealthCheck healthCheck = context.getBean(ProviderHealthCheck.class);
                    assertThat(healthCheck.areAllProvidersHealthy()).isTrue();
                    assertThat(healthCheck.getTotalProviderCount()).isZero();
                });
    }

    @Test
    void healthCheckShouldNotBeCreatedWhenDisabled() {
        this.contextRunner
                .withPropertyValues(
                        "quickpay.providers.enabled=true",
                        "quickpay.providers.health-check-enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ProviderHealthCheck.class);
                });
    }

    @Test
    void userCanOverrideRestClientBuilder() {
        this.contextRunner
                .withPropertyValues("quickpay.providers.enabled=true")
                .withUserConfiguration(CustomRestClientConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(RestClient.Builder.class);
                    RestClient.Builder builder = context.getBean(RestClient.Builder.class);
                    assertThat(builder).isNotNull();
                });
    }

    /**
     * Custom configuration for testing user overrides
     */
    static class CustomRestClientConfig {
        @Bean
        RestClient.Builder customRestClientBuilder() {
            return RestClient.builder();
        }
    }
}
