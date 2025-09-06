package com.quickpay.logging.service;

import com.quickpay.logging.autoconfigure.QuickPayLoggingProperties;
import com.quickpay.logging.domain.PaymentFailedEvent;
import com.quickpay.logging.domain.PaymentSuccessEvent;
import com.quickpay.logging.formatter.PaymentFailedEventEcsFormatter;
import com.quickpay.logging.formatter.PaymentSuccessEventEcsFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.Map;

/**
 * Type-safe payment logging service that ensures consistent payment event logging
 * across all QuickPay services. This service automatically applies ECS formatting
 * and PII masking through the configured StructuredLogFormatter.
 */
public class PaymentLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentLogger.class);

    private final QuickPayLoggingProperties quickPayLoggingProperties;
    private final PaymentSuccessEventEcsFormatter paymentSuccessEventEcsFormatter;
    private final PaymentFailedEventEcsFormatter paymentFailedEventEcsFormatter;
    
    public PaymentLogger(QuickPayLoggingProperties quickPayLoggingProperties, PaymentSuccessEventEcsFormatter paymentSuccessEventEcsFormatter, PaymentFailedEventEcsFormatter paymentFailedEventEcsFormatter) {
        this.quickPayLoggingProperties = quickPayLoggingProperties;
        this.paymentSuccessEventEcsFormatter = paymentSuccessEventEcsFormatter;
        this.paymentFailedEventEcsFormatter = paymentFailedEventEcsFormatter;
    }
    
    /**
     * Log a successful payment event.
     * This method ensures all payment success events are logged consistently
     * with proper ECS formatting.
     * 
     * @param event The payment success event to log
     */
    public void logSuccess(PaymentSuccessEvent event) {
        if(quickPayLoggingProperties.ecs().enabled()) {
            String successLogEvent = paymentSuccessEventEcsFormatter.format(event);
            logger.info(successLogEvent);
            return;
        }
        logger.info(event.toString());

    }
    
    /**
     * Log a failed payment event.
     * This method ensures all payment failure events are logged consistently
     * with proper ECS formatting.
     * 
     * @param event The payment failed event to log
     */
    public void logFailure(PaymentFailedEvent event) {
        if(quickPayLoggingProperties.ecs().enabled()) {
            String failedLogEvent = paymentFailedEventEcsFormatter.format(event);
            logger.error(failedLogEvent);
        }
        else {
            logger.error(event.toString());
        }
    }
}