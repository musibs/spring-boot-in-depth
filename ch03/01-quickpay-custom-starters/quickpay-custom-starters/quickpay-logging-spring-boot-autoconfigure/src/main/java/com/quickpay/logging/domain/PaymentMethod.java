package com.quickpay.logging.domain;

public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    ACH("ACH Transfer"),
    WIRE_TRANSFER("Wire Transfer"),
    DIGITAL_WALLET("Digital Wallet"),
    BANK_TRANSFER("Bank Transfer"),
    CHECK("Check");
    
    private final String displayName;
    
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isCardBased() {
        return this == CREDIT_CARD || this == DEBIT_CARD;
    }
    
    public boolean isElectronic() {
        return this != CHECK;
    }
}