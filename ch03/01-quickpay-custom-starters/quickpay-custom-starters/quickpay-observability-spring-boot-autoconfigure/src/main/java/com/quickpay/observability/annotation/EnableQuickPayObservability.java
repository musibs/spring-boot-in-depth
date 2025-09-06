package com.quickpay.observability.annotation;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportAutoConfiguration(com.quickpay.observability.autoconfigure.ObservabilityAutoConfiguration.class)
public @interface EnableQuickPayObservability {
}