package com.quickpay.payment.repository;

import com.quickpay.payment.domain.Payment;
import com.quickpay.payment.domain.PaymentId;
import com.quickpay.payment.domain.CustomerId;
import com.quickpay.payment.domain.MerchantId;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    
    Payment save(Payment payment);
    
    Optional<Payment> findById(PaymentId paymentId);
    
    List<Payment> findByCustomerId(CustomerId customerId);
    
    List<Payment> findByMerchantId(MerchantId merchantId);
    
    void deleteById(PaymentId paymentId);
}