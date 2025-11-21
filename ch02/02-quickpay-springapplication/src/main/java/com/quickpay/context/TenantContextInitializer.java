package com.quickpay.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.Map;

public class TenantContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextInitializer.class);
    private record TenantConfig(String apiKey, String merchantId, String defaultCurrency) {}

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        setupMultiTenantPaymentConfigs(applicationContext);
    }

    private void setupMultiTenantPaymentConfigs(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();

        try {
            Map<String, TenantConfig> tenantConfigs = loadTenantConfigs();
            Map<String, Object> propertyMap = new HashMap<>();

            for (Map.Entry<String, TenantConfig> entry : tenantConfigs.entrySet()) {
                String tenantId = entry.getKey();
                TenantConfig config = entry.getValue();

                String baseKey = "quickpay.tenant." + tenantId;
                propertyMap.put(baseKey + ".api-key", config.apiKey());
                propertyMap.put(baseKey + ".merchant-id", config.merchantId());
                propertyMap.put(baseKey + ".currency", config.defaultCurrency());
                logger.debug("Configured tenant: {}", tenantId);
            }

            propertySources.addFirst(new MapPropertySource("quickpay-tenant-configs", propertyMap));
            logger.info("Successfully configured {} tenant payment settings", tenantConfigs.size());

        } catch (Exception e) {
            logger.error("Failed to load tenant configurations, falling back to defaults", e);
            //setupDefaultTenantConfig(propertySources);
        }
    }

    private Map<String, TenantConfig> loadTenantConfigs() {
        // In a real implementation, this would connect to a database or config service
        // For now, simulating with mock data
        Map<String, TenantConfig> configs = new HashMap<>();
        configs.put("corp1", new TenantConfig("DummyKey1", "M-123", "USD"));
        configs.put("corp2", new TenantConfig("DummyKey2", "M-234", "AUD"));
        return configs;
    }
}


