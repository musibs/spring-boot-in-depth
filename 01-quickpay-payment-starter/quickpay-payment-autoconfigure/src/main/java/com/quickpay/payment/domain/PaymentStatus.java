package com.quickpay.payment.domain;

/**
 * Represents the lifecycle status of a payment transaction.
 * 
 * Payment status transitions:
 * PENDING -> PROCESSING -> COMPLETED/FAILED
 * COMPLETED -> REFUNDED (optional)
 */
public enum PaymentStatus {
    /** Payment created but not yet processed */
    PENDING, 
    
    /** Payment is being processed */
    PROCESSING, 
    
    /** Payment successfully completed */
    COMPLETED, 
    
    /** Payment failed */
    FAILED, 
    
    /** Payment was refunded */
    REFUNDED
}