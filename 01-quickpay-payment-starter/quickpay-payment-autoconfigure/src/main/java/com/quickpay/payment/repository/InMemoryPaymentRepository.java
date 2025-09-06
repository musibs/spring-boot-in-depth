package com.quickpay.payment.repository;

import com.quickpay.payment.domain.Payment;
import com.quickpay.payment.domain.PaymentId;
import com.quickpay.payment.domain.CustomerId;
import com.quickpay.payment.domain.MerchantId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryPaymentRepository implements PaymentRepository {
    
    private final Map<PaymentId, Payment> payments = new ConcurrentHashMap<>();
    
    @Override
    public Payment save(Payment payment) {
        payments.put(payment.paymentId(), payment);
        return payment;
    }
    
    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }
    
    @Override
    public List<Payment> findByCustomerId(CustomerId customerId) {
        return payments.values().stream()
                .filter(payment -> payment.customerId().equals(customerId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findByMerchantId(MerchantId merchantId) {
        return payments.values().stream()
                .filter(payment -> payment.merchantId().equals(merchantId))
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(PaymentId paymentId) {
        payments.remove(paymentId);
    }
}