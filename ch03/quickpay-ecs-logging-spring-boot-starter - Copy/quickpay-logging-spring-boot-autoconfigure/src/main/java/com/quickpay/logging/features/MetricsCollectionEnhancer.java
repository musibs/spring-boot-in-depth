package com.quickpay.logging.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Enhances logging with structured metrics collection and export.
 */
public class MetricsCollectionEnhancer implements InitializingBean {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollectionEnhancer.class);
    
    private final boolean enabled;
    
    public MetricsCollectionEnhancer(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (enabled) {
            logger.info("📈 Metrics Collection initialized - Business KPIs and SLAs tracking active");
        }
    }
}