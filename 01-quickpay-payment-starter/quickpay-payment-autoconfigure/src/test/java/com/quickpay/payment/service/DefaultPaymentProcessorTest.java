package com.quickpay.payment.service;

import com.quickpay.payment.domain.*;
import com.quickpay.payment.repository.InMemoryPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultPaymentProcessorTest {
    
    private DefaultPaymentProcessor paymentProcessor;
    private InMemoryPaymentRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryPaymentRepository();
        paymentProcessor = new DefaultPaymentProcessor(repository);
    }
    
    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        Payment payment = createTestPayment();
        
        CompletableFuture<Payment> result = paymentProcessor.processPayment(payment);
        Payment processedPayment = result.get();
        
        assertThat(processedPayment.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(processedPayment.processedAt()).isPresent();
    }
    
    @Test
    void shouldGetPaymentById() {
        Payment payment = createTestPayment();
        repository.save(payment);
        
        Optional<Payment> result = paymentProcessor.getPayment(payment.paymentId());
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(payment);
    }
    
    @Test
    void shouldRefundCompletedPayment() throws Exception {
        Payment payment = createTestPayment();
        CompletableFuture<Payment> processedFuture = paymentProcessor.processPayment(payment);
        Payment processedPayment = processedFuture.get();
        
        Payment refundedPayment = paymentProcessor.refundPayment(
            processedPayment.paymentId(), 
            "Customer request"
        );
        
        assertThat(refundedPayment.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(refundedPayment.failureReason()).contains("Customer request");
    }
    
    @Test
    void shouldThrowExceptionWhenRefundingNonExistentPayment() {
        PaymentId nonExistentId = PaymentId.generate();
        
        assertThatThrownBy(() -> paymentProcessor.refundPayment(nonExistentId, "Test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment not found");
    }
    
    private Payment createTestPayment() {
        return Payment.createPending(
            PaymentId.generate(),
            CustomerId.of("customer-123"),
            MerchantId.of("merchant-456"),
            Money.of(100.0, Currency.getInstance("USD"))
        );
    }
}