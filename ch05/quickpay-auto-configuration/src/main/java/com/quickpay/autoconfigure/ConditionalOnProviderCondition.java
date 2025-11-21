package com.quickpay.autoconfigure;

import com.quickpay.domain.PaymentProvider;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * Condition implementation for {@link ConditionalOnProvider}.
 * Checks if a specific payment provider is enabled via configuration properties.
 * <p>
 * Demonstrates:
 * - Custom condition implementation extending SpringBootCondition
 * - Property-based conditional logic
 * - ConditionOutcome and ConditionMessage usage
 */
class ConditionalOnProviderCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Get the annotation attributes
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnProvider.class.getName());

        if (attributes == null) {
            return ConditionOutcome.noMatch(
                    ConditionMessage.forCondition(ConditionalOnProvider.class)
                            .because("annotation attributes not found"));
        }

        // Get the provider from annotation
        PaymentProvider provider = (PaymentProvider) attributes.get("value");
        boolean matchIfMissing = (Boolean) attributes.get("matchIfMissing");

        // Build the property name to check: quickpay.providers.{provider}.enabled
        String propertyName = "quickpay.providers." + provider.name().toLowerCase() + ".enabled";

        // Check the property value
        String propertyValue = context.getEnvironment().getProperty(propertyName);

        // Determine match outcome
        boolean matches;
        String reason;

        if (propertyValue == null) {
            matches = matchIfMissing;
            reason = matchIfMissing
                    ? "property '" + propertyName + "' not found, matching because matchIfMissing=true"
                    : "property '" + propertyName + "' not found";
        } else {
            matches = "true".equalsIgnoreCase(propertyValue);
            reason = "property '" + propertyName + "' has value '" + propertyValue + "'";
        }

        ConditionMessage message = ConditionMessage.forCondition(ConditionalOnProvider.class, provider)
                .because(reason);

        return matches
                ? ConditionOutcome.match(message)
                : ConditionOutcome.noMatch(message);
    }
}
