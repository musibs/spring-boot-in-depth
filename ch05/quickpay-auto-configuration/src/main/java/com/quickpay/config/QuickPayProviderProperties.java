package com.quickpay.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Type-safe configuration properties for QuickPay payment providers.
 * Uses Java Record for immutable, concise configuration.
 * <p>
 * Demonstrates:
 * <ul>
 *   <li>ConfigurationProperties with Records (Spring Boot 3+)</li>
 *   <li>Jakarta validation annotations</li>
 *   <li>Nested configuration properties</li>
 *   <li>Default values with @DefaultValue</li>
 * </ul>
 *
 * @param enabled     whether provider integration is enabled
 * @param stripe      Stripe provider configuration
 * @param paypal      PayPal provider configuration
 * @param common      common configuration for all providers
 */
@ConfigurationProperties(prefix = "quickpay.providers")
@Validated
public record QuickPayProviderProperties(

        @DefaultValue("true")
        boolean enabled,

        StripeProperties stripe,

        PaypalProperties paypal,

        @NotNull
        CommonProperties common
) {

    /**
     * Default constructor with default values
     */
    public QuickPayProviderProperties {
        common = common != null ? common : new CommonProperties(Duration.ofSeconds(30), 3);
    }

    /**
     * Stripe-specific configuration properties.
     *
     * @param enabled whether Stripe integration is enabled
     * @param apiKey  Stripe API key
     * @param baseUrl Stripe API base URL
     */
    public record StripeProperties(

            @DefaultValue("false")
            boolean enabled,

            @Pattern(regexp = "^(sk_|pk_).*", message = "Stripe API key must start with 'sk_' or 'pk_'")
            String apiKey,

            @DefaultValue("https://api.stripe.com")
            String baseUrl
    ) {
    }

    /**
     * PayPal-specific configuration properties.
     *
     * @param enabled      whether PayPal integration is enabled
     * @param clientId     PayPal client ID
     * @param clientSecret PayPal client secret
     * @param baseUrl      PayPal API base URL
     * @param mode         PayPal mode (sandbox or live)
     */
    public record PaypalProperties(

            @DefaultValue("false")
            boolean enabled,

            @NotBlank(message = "PayPal client ID is required when PayPal is enabled")
            String clientId,

            @NotBlank(message = "PayPal client secret is required when PayPal is enabled")
            String clientSecret,

            @DefaultValue("https://api.paypal.com")
            String baseUrl,

            @DefaultValue("sandbox")
            @Pattern(regexp = "^(sandbox|live)$", message = "PayPal mode must be 'sandbox' or 'live'")
            String mode
    ) {
    }

    /**
     * Common configuration for all providers.
     *
     * @param timeout       request timeout
     * @param retryAttempts number of retry attempts on failure
     */
    public record CommonProperties(

            @NotNull
            @DefaultValue("30s")
            Duration timeout,

            @Positive
            @DefaultValue("3")
            int retryAttempts
    ) {
    }
}
