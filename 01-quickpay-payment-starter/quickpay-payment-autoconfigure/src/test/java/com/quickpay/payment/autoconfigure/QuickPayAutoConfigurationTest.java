package com.quickpay.payment.autoconfigure;

import com.quickpay.payment.repository.PaymentRepository;
import com.quickpay.payment.service.PaymentProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class QuickPayAutoConfigurationTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(QuickPayAutoConfiguration.class));
    
    @Test
    void shouldConfigurePaymentBeansWhenEnabled() {
        contextRunner
                .withPropertyValues("quickpay.payment.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(PaymentRepository.class);
                    assertThat(context).hasSingleBean(PaymentProcessor.class);
                });
    }
    
    @Test
    void shouldNotConfigurePaymentBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("quickpay.payment.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PaymentRepository.class);
                    assertThat(context).doesNotHaveBean(PaymentProcessor.class);
                });
    }
    
    @Test
    void shouldConfigurePaymentBeansByDefault() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(PaymentRepository.class);
                    assertThat(context).hasSingleBean(PaymentProcessor.class);
                });
    }
}