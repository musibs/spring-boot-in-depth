package com.quickpay.logging.domain;

public enum FailureReason {
    INSUFFICIENT_FUNDS("Insufficient funds", "INSUFFICIENT_FUNDS"),
    INVALID_CARD("Invalid card number", "INVALID_CARD"),
    EXPIRED_CARD("Card expired", "EXPIRED_CARD"),
    NETWORK_ERROR("Network communication error", "NETWORK_ERROR"),
    FRAUD_DETECTED("Fraudulent activity detected", "FRAUD_DETECTED"),
    MERCHANT_BLOCKED("Merchant account blocked", "MERCHANT_BLOCKED"),
    LIMIT_EXCEEDED("Transaction limit exceeded", "LIMIT_EXCEEDED"),
    INVALID_CVV("Invalid CVV", "INVALID_CVV"),
    CARD_BLOCKED("Card blocked by issuer", "CARD_BLOCKED"),
    SYSTEM_ERROR("Internal system error", "SYSTEM_ERROR"),
    TIMEOUT("Transaction timeout", "TIMEOUT"),
    DUPLICATE_TRANSACTION("Duplicate transaction", "DUPLICATE_TRANSACTION");
    
    private final String description;
    private final String code;
    
    FailureReason(String description, String code) {
        this.description = description;
        this.code = code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCode() {
        return code;
    }
    
    public boolean isRetryable() {
        return switch (this) {
            case NETWORK_ERROR, TIMEOUT, SYSTEM_ERROR -> true;
            case INSUFFICIENT_FUNDS, INVALID_CARD, EXPIRED_CARD, 
                 FRAUD_DETECTED, MERCHANT_BLOCKED, INVALID_CVV, 
                 CARD_BLOCKED, DUPLICATE_TRANSACTION, LIMIT_EXCEEDED -> false;
        };
    }
    
    public boolean requiresCustomerAction() {
        return switch (this) {
            case INSUFFICIENT_FUNDS, INVALID_CARD, EXPIRED_CARD, 
                 INVALID_CVV, LIMIT_EXCEEDED -> true;
            case NETWORK_ERROR, FRAUD_DETECTED, MERCHANT_BLOCKED, 
                 CARD_BLOCKED, SYSTEM_ERROR, TIMEOUT, DUPLICATE_TRANSACTION -> false;
        };
    }
}