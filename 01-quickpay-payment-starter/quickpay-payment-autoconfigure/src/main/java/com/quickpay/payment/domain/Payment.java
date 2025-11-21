package com.quickpay.payment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.math.BigDecimal;

/**
 * Represents a payment transaction in the QuickPay system.
 * 
 * This is an immutable record that captures all essential payment information
 * including parties involved, amount, status, and timestamps. Uses the Factory
 * pattern for creation and immutable state transitions.
 */
public record Payment(
    PaymentId paymentId,
    CustomerId customerId,
    MerchantId merchantId,
    Money amount,
    PaymentStatus status,
    Instant createdAt,
    Optional<Instant> processedAt,
    Optional<String> failureReason
) {
    public Payment {
        Objects.requireNonNull(paymentId, "Payment ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(merchantId, "Merchant ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(createdAt, "Created date cannot be null");
        
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }
    
    /**
     * Factory method to create a new pending payment.
     * @param id unique payment identifier
     * @param customerId customer making the payment
     * @param merchantId merchant receiving the payment
     * @param amount payment amount
     * @return new pending payment
     */
    public static Payment createPending(PaymentId id, CustomerId customerId, 
                                       MerchantId merchantId, Money amount) {
        return new Payment(id, customerId, merchantId, amount, 
                         PaymentStatus.PENDING, Instant.now(), 
                         Optional.empty(), Optional.empty());
    }
    
    /**
     * Marks payment as processing. Only pending payments can be processed.
     * @return new payment instance with PROCESSING status
     * @throws IllegalStateException if payment is not pending
     */
    public Payment markAsProcessing() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Can only process pending payments");
        }
        return new Payment(paymentId, customerId, merchantId, amount, 
                         PaymentStatus.PROCESSING, createdAt, 
                         Optional.empty(), Optional.empty());
    }
    
    /**
     * Marks payment as completed. Only processing payments can be completed.
     * @return new payment instance with COMPLETED status
     * @throws IllegalStateException if payment is not processing
     */
    public Payment markAsCompleted() {
        if (status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Can only complete processing payments");
        }
        return new Payment(paymentId, customerId, merchantId, amount, 
                         PaymentStatus.COMPLETED, createdAt, 
                         Optional.of(Instant.now()), Optional.empty());
    }
    
    /**
     * Marks payment as failed with a reason.
     * @param reason failure reason
     * @return new payment instance with FAILED status
     */
    public Payment markAsFailed(String reason) {
        return new Payment(paymentId, customerId, merchantId, amount, 
                         PaymentStatus.FAILED, createdAt, 
                         Optional.of(Instant.now()), Optional.of(reason));
    }
}