package com.quickpay.payment.annotation;

import com.quickpay.payment.autoconfigure.QuickPayAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables QuickPay payment processing functionality.
 * 
 * This annotation imports the QuickPayAutoConfiguration class to set up
 * payment processing beans and configuration. It provides a declarative
 * way to enable payment features in Spring Boot applications.
 * 
 * Usage:
 * <pre>
 * &#64;SpringBootApplication
 * &#64;EnableQuickPayPayments
 * public class PaymentApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(PaymentApplication.class, args);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(QuickPayAutoConfiguration.class)
public @interface EnableQuickPayPayments {
    
    /**
     * Payment providers to enable.
     * @return array of provider names, defaults to ["mock"]
     */
    String[] providers() default {"mock"};
    
    /**
     * Whether to enable payment security features.
     * @return true to enable security, false otherwise
     */
    boolean enableSecurity() default true;
    
    /**
     * Whether to enable payment observability features.
     * @return true to enable observability, false otherwise
     */
    boolean enableObservability() default true;
}