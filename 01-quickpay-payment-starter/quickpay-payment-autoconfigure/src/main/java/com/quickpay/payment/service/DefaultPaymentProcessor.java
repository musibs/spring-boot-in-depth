package com.quickpay.payment.service;

import com.quickpay.payment.domain.Payment;
import com.quickpay.payment.domain.PaymentId;
import com.quickpay.payment.domain.PaymentStatus;
import com.quickpay.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of PaymentProcessor.
 * 
 * Provides a mock payment processing implementation that simulates
 * real payment provider interactions with artificial delays and
 * state management.
 */
public class DefaultPaymentProcessor implements PaymentProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultPaymentProcessor.class);
    
    private final PaymentRepository paymentRepository;
    
    public DefaultPaymentProcessor(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    @Override
    @CacheEvict(value = "payments", key = "#payment.paymentId().value()")
    public CompletableFuture<Payment> processPayment(Payment payment) {
        logger.info("Processing payment: {}", payment.paymentId().value());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Payment processingPayment = payment.markAsProcessing();
                paymentRepository.save(processingPayment);
                Payment completedPayment = processingPayment.markAsCompleted();
                paymentRepository.save(completedPayment);
                
                logger.info("Payment processed successfully: {}", payment.paymentId().value());
                return completedPayment;
                
            } catch (Exception e) {
                Payment failedPayment = payment.markAsFailed("Unexpected error: " + e.getMessage());
                paymentRepository.save(failedPayment);
                logger.error("Payment processing failed: {}", payment.paymentId().value(), e);
                return failedPayment;
            }
        });
    }
    
    @Override
    @Cacheable(value = "payments", key = "#paymentId.value()")
    public Optional<Payment> getPayment(PaymentId paymentId) {
        logger.debug("Retrieving payment from repository: {}", paymentId.value());
        return paymentRepository.findById(paymentId);
    }
    
    @Override
    @CacheEvict(value = "payments", key = "#paymentId.value()")
    public Payment refundPayment(PaymentId paymentId, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Payment not found: " + paymentId.value());
        }
        
        Payment payment = paymentOpt.get();
        if (payment.status() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Can only refund completed payments");
        }
        
        Payment refundedPayment = new Payment(
            payment.paymentId(),
            payment.customerId(),
            payment.merchantId(),
            payment.amount(),
            PaymentStatus.REFUNDED,
            payment.createdAt(),
            payment.processedAt(),
            Optional.of(reason)
        );
        
        logger.info("Processing refund for payment: {}", paymentId.value());
        return paymentRepository.save(refundedPayment);
    }
}