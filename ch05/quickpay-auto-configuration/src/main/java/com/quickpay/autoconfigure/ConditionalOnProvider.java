package com.quickpay.autoconfigure;

import com.quickpay.domain.PaymentProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Custom conditional annotation for payment provider beans.
 * Conditionally creates beans based on provider-specific properties.
 * <p>
 * This is a meta-annotation that combines property checking for a specific provider.
 * Demonstrates creating domain-specific conditional annotations for better readability.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @Bean
 * @ConditionalOnProvider(PaymentProvider.STRIPE)
 * public StripeClient stripeClient() {
 *     // ...
 * }
 * }
 * </pre>
 *
 * @see ConditionalOnProperty
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ConditionalOnProviderCondition.class)
public @interface ConditionalOnProvider {

    /**
     * The payment provider that must be enabled for this condition to match.
     *
     * @return the payment provider
     */
    PaymentProvider value();

    /**
     * Whether to match if the property is missing.
     * Defaults to false (property must be explicitly set to true).
     *
     * @return true if should match when property is missing
     */
    boolean matchIfMissing() default false;
}
