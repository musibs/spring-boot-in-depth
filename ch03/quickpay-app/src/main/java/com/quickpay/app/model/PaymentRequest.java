package com.quickpay.app.model;

import com.quickpay.logging.domain.Money;
import com.quickpay.logging.domain.PaymentMethod;

import java.math.BigDecimal;
import java.util.Objects;

public record PaymentRequest(
        String customerId,
        Money money,
        String paymentDescription,
        PaymentMethod paymentMethod
) {
    public PaymentRequest {
        Objects.requireNonNull(money, "Amount cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(paymentDescription, "Payment Description cannot be null");
        Objects.requireNonNull(paymentMethod, "Payment Method cannot be null");
    }

    public boolean isHighValue() {
        return money.amount().compareTo(BigDecimal.valueOf(10000)) >= 0;
    }

    public boolean isNegativeValue() {
        return money.amount().compareTo(BigDecimal.valueOf(0)) <= 0;
    }

}