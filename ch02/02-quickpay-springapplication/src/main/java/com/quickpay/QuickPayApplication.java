package com.quickpay;

import com.quickpay.context.TenantContextInitializer;
import com.quickpay.listener.QuickPayApplicationReadyListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuickPayApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(QuickPayApplication.class);
		springApplication.addInitializers(new TenantContextInitializer());
		springApplication.addListeners(new QuickPayApplicationReadyListener());
		springApplication.run(args);
	}
}
