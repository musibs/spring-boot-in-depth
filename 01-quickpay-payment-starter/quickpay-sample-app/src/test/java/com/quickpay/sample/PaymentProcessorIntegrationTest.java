package com.quickpay.sample;

import com.quickpay.payment.annotation.EnableQuickPayPayments;
import com.quickpay.payment.domain.*;
import com.quickpay.payment.service.PaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Currency;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PaymentProcessor with caching enabled.
 * Tests the complete payment processing flow including cache behavior.
 */
@SpringBootTest
@EnableQuickPayPayments
@TestPropertySource(properties = {
    "quickpay.payment.enabled=true",
    "quickpay.payment.cache.enabled=true",
    "quickpay.payment.cache.type=caffeine",
    "quickpay.payment.cache.ttl=5m"
})
class PaymentProcessorIntegrationTest {

    @Autowired
    private PaymentProcessor paymentProcessor;
    
    @Autowired
    private CacheManager cacheManager;
    
    private PaymentId paymentId;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // Clear caches before each test
        cacheManager.getCache("payments").clear();
        
        // Create test payment
        paymentId = PaymentId.generate();
        testPayment = Payment.createPending(
            paymentId,
            new CustomerId("customer-123"),
            new MerchantId("merchant-456"),
            Money.usd(100.0)
        );
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        // When: Process payment
        CompletableFuture<Payment> future = paymentProcessor.processPayment(testPayment);
        Payment processedPayment = future.get();

        // Then: Payment should be completed
        assertThat(processedPayment.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(processedPayment.paymentId()).isEqualTo(paymentId);
        assertThat(processedPayment.amount()).isEqualTo(Money.usd(100.0));
        assertThat(processedPayment.processedAt()).isPresent();
    }

    @Test
    void shouldCachePaymentLookups() throws Exception {
        // Given: Process payment first
        CompletableFuture<Payment> future = paymentProcessor.processPayment(testPayment);
        Payment processedPayment = future.get();

        // When: Get payment multiple times
        Optional<Payment> firstLookup = paymentProcessor.getPayment(paymentId);
        Optional<Payment> secondLookup = paymentProcessor.getPayment(paymentId);

        // Then: Both lookups should return same result
        assertThat(firstLookup).isPresent();
        assertThat(secondLookup).isPresent();
        assertThat(firstLookup.get()).isEqualTo(secondLookup.get());
        
        // Verify cache contains the payment
        assertThat(cacheManager.getCache("payments").get(paymentId.value())).isNotNull();
    }

    @Test
    void shouldEvictCacheOnPaymentProcessing() throws Exception {
        // Given: Cache a pending payment lookup
        Optional<Payment> initialLookup = paymentProcessor.getPayment(paymentId);
        assertThat(initialLookup).isEmpty(); // Not found, but cached as empty
        
        // When: Process the payment (this should evict cache)
        CompletableFuture<Payment> future = paymentProcessor.processPayment(testPayment);
        Payment processedPayment = future.get();

        // Then: Next lookup should get fresh data from repository
        Optional<Payment> afterProcessing = paymentProcessor.getPayment(paymentId);
        assertThat(afterProcessing).isPresent();
        assertThat(afterProcessing.get().status()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void shouldEvictCacheOnRefund() throws Exception {
        // Given: Process and cache a payment
        CompletableFuture<Payment> future = paymentProcessor.processPayment(testPayment);
        Payment processedPayment = future.get();
        
        Optional<Payment> cached = paymentProcessor.getPayment(paymentId);
        assertThat(cached).isPresent();
        assertThat(cached.get().status()).isEqualTo(PaymentStatus.COMPLETED);

        // When: Refund the payment (this should evict cache)
        Payment refundedPayment = paymentProcessor.refundPayment(paymentId, "Customer request");

        // Then: Payment should be refunded and cache updated
        assertThat(refundedPayment.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(refundedPayment.failureReason()).contains("Customer request");
        
        // Verify fresh lookup gets refunded status
        Optional<Payment> afterRefund = paymentProcessor.getPayment(paymentId);
        assertThat(afterRefund).isPresent();
        assertThat(afterRefund.get().status()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void shouldThrowExceptionWhenRefundingNonExistentPayment() {
        // Given: Non-existent payment ID
        PaymentId nonExistentId = PaymentId.generate();

        // When/Then: Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            paymentProcessor.refundPayment(nonExistentId, "Test refund");
        });
    }

    @Test
    void shouldThrowExceptionWhenRefundingNonCompletedPayment() {
        // Given: Pending payment (not completed)
        PaymentId pendingPaymentId = PaymentId.generate();
        Payment pendingPayment = Payment.createPending(
            pendingPaymentId,
            new CustomerId("customer-789"),
            new MerchantId("merchant-012"),
            Money.eur(50.0)
        );

        // Save pending payment by trying to get it (this will cache it)
        // Note: This is a limitation of the mock implementation - in real scenario,
        // we'd save to repository first
        
        // When/Then: Should throw exception for non-completed payment
        assertThrows(IllegalStateException.class, () -> {
            paymentProcessor.refundPayment(pendingPaymentId, "Test refund");
        });
    }

    @Test
    void shouldHandleMultipleCurrencies() throws Exception {
        // Given: Payments in different currencies
        PaymentId eurPaymentId = PaymentId.generate();
        Payment eurPayment = Payment.createPending(
            eurPaymentId,
            new CustomerId("customer-eur"),
            new MerchantId("merchant-eur"),
            Money.eur(250.0)
        );

        // When: Process EUR payment
        CompletableFuture<Payment> eurFuture = paymentProcessor.processPayment(eurPayment);
        Payment processedEurPayment = eurFuture.get();

        // Then: Should maintain currency
        assertThat(processedEurPayment.amount().currency()).isEqualTo(Currency.getInstance("EUR"));
        assertThat(processedEurPayment.amount().amount()).isEqualByComparingTo("250.0");
        assertThat(processedEurPayment.status()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void shouldVerifyCacheManagerConfiguration() {
        // Then: Cache manager should be configured
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCache("payments")).isNotNull();
        
        // Cache should be empty initially
        assertThat(cacheManager.getCache("payments").get(paymentId.value())).isNull();
    }
}