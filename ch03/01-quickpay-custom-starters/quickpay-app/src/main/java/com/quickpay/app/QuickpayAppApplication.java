package com.quickpay.app;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuickpayAppApplication {

	private static final Logger logger = LoggerFactory.getLogger(QuickpayAppApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(QuickpayAppApplication.class, args);
	}

	/**
	 * Application runner to demonstrate QuickPay logging starter features.
	 *
	 * @return an ApplicationRunner that processes sample payments
	 */
	@Bean
	ApplicationRunner applicationRunner() {
		return _ -> {
			logger.atInfo()
			.addKeyValue("transaction", "Txn_123443211234")
			.addKeyValue("customer", "CIF_23456543")
			.addKeyValue("amount", 100)
			.addKeyValue("iban", "FI34567889901")
			.addKeyValue("card", "123456789090")
			.addKeyValue("processingTimeMs", Duration.ofMillis(200))
			.log("Payment processed successfully");
		};
	}
}