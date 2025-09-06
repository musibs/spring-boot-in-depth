package com.quickpay.payment.service;

import com.quickpay.payment.domain.Payment;
import com.quickpay.payment.domain.PaymentId;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Core interface for payment processing operations.
 * 
 * Defines the contract for processing payments asynchronously,
 * retrieving payment information, and handling refunds.
 */
public interface PaymentProcessor {
    
    /**
     * Processes a payment asynchronously.
     * @param payment the payment to process
     * @return CompletableFuture with the processed payment result
     */
    CompletableFuture<Payment> processPayment(Payment payment);
    
    /**
     * Retrieves a payment by its ID.
     * @param paymentId the payment identifier
     * @return Optional containing the payment if found
     */
    Optional<Payment> getPayment(PaymentId paymentId);
    
    /**
     * Processes a refund for a completed payment.
     * @param paymentId the payment to refund
     * @param reason refund reason
     * @return the refunded payment
     */
    Payment refundPayment(PaymentId paymentId, String reason);
}