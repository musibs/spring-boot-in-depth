package com.quickpay.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Externalized HTTP client configuration imported by PaymentProviderAutoConfiguration.
 * <p>
 * Demonstrates:
 * - Externalizing cross-cutting concerns via @Import
 * - ConditionalOnMissingBean for user override capability
 * - Configuration being applied from properties
 */
@Configuration(proxyBeanMethods = false)
public class HttpClientConfiguration {

    /**
     * Provides a default RestClient.Builder for provider clients.
     * Uses JDK HttpClient for better performance.
     * <p>
     * ConditionalOnMissingBean allows users to provide their own RestClient.Builder
     * if they need custom configuration.
     *
     * @param properties QuickPay properties for timeout configuration
     * @return configured RestClient.Builder
     */
    @Bean
    @ConditionalOnMissingBean
    RestClient.Builder restClientBuilder(QuickPayProviderProperties properties) {
        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(properties.common().timeout());

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeaders(headers -> {
                    headers.set("User-Agent", "QuickPay/1.0");
                    headers.set("Content-Type", "application/json");
                    headers.set("Accept", "application/json");
                });
    }

    /**
     * Provides a general-purpose RestClient bean.
     *
     * @param builder the RestClient.Builder
     * @return configured RestClient
     */
    @Bean
    @ConditionalOnMissingBean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}
