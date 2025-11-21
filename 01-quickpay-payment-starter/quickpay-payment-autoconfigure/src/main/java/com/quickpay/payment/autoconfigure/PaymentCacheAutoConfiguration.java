package com.quickpay.payment.autoconfigure;

import com.quickpay.payment.config.QuickPayProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.List;

/**
 * Auto-configuration for payment caching.
 * 
 * Sets up caching infrastructure for payment operations to improve
 * performance by reducing database lookups for frequently accessed payments.
 * 
 * Caching is enabled when:
 * - quickpay.payment.cache.enabled=true (default)
 * - Caffeine is on the classpath
 * 
 * Provides cache region: "payments"
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "quickpay.payment.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableCaching
public class PaymentCacheAutoConfiguration {
    
    /**
     * Caffeine-specific cache configuration.
     * Only activated when Caffeine is available on classpath.
     */
    @Configuration
    @ConditionalOnClass(Caffeine.class)
    @ConditionalOnProperty(prefix = "quickpay.payment.cache", name = "type", havingValue = "caffeine", matchIfMissing = true)
    static class CaffeineConfiguration {
        
        /**
         * Configures Caffeine cache manager for payment caching.
         * Uses TTL from properties and sets up payment-specific cache regions.
         */
        @Bean
        @ConditionalOnMissingBean
        public CacheManager cacheManager(QuickPayProperties properties) {
            CaffeineCacheManager cacheManager = new CaffeineCacheManager();
            
            Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(properties.cache().ttl());
            
            cacheManager.setCaffeine(caffeine);
            cacheManager.setCacheNames(List.of("payments"));
            
            return cacheManager;
        }
    }
}