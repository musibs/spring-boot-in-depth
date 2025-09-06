package com.quickpay.logging.annotation;

import com.quickpay.logging.autoconfigure.QuickPayLoggingAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables QuickPay payment logging with ECS format

 * @author Somnath Musib
 * @since 1.0.0
 * @see QuickPayLoggingAutoConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({QuickPayLoggingAutoConfiguration.class})
public @interface EnableQuickPayLogging {
}