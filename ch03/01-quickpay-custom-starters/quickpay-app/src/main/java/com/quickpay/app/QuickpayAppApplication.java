package com.quickpay.app;

import com.quickpay.app.model.PaymentRequest;
import com.quickpay.app.service.PaymentProcessingService;
import com.quickpay.logging.annotation.EnableQuickPayLogging;
import com.quickpay.logging.domain.*;
import com.quickpay.logging.service.PaymentLogger;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Duration;

@SpringBootApplication
@EnableQuickPayLogging
public class QuickpayAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickpayAppApplication.class, args);
    }
    /**
     * Application runner to simulate a payment processing request.
     *
     * @param paymentProcessingService the service to process payments
     * @return an ApplicationRunner that processes a sample payment
     */
    @Bean
    ApplicationRunner applicationRunner(PaymentProcessingService paymentProcessingService) {
        return args -> {
            paymentProcessingService.processPayment(
                    new PaymentRequest(
                            "customer_123",
                            Money.of("100.00", "USD"),
                            "Payment for order #12345",
                            PaymentMethod.CREDIT_CARD
                    ),
                    "txn_" + java.util.UUID.randomUUID().toString().replace("-", ""
            ));
        };
    }
}
