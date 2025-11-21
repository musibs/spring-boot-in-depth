package com.quickpay.app;

import com.quickpay.logging.annotation.EnableQuickPayLogging;
import com.quickpay.observability.annotation.EnableQuickPayObservability;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableQuickPayLogging
@EnableQuickPayObservability
public class QuickpayAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickpayAppApplication.class, args);
    }

}
