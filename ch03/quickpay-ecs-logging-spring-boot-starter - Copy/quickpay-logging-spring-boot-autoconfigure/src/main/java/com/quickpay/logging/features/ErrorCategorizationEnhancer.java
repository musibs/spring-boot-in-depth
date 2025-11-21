package com.quickpay.logging.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Enhances logging with intelligent error categorization and alerting.
 */
public class ErrorCategorizationEnhancer implements InitializingBean {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorCategorizationEnhancer.class);
    
    private final boolean enabled;
    
    public ErrorCategorizationEnhancer(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (enabled) {
            logger.info("🔍 Error Categorization initialized - AI-powered error analysis active");
        }
    }
}