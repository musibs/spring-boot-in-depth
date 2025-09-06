package com.quickpay.sample;

import com.quickpay.payment.annotation.EnableQuickPayPayments;
import com.quickpay.payment.domain.*;
import com.quickpay.payment.repository.PaymentRepository;
import com.quickpay.payment.service.PaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Demonstrates cache behavior in PaymentProcessor.
 * Uses Spy to verify repository call patterns and cache effectiveness.
 */
@SpringBootTest
@EnableQuickPayPayments
@TestPropertySource(properties = {
    "quickpay.payment.enabled=true",
    "quickpay.payment.cache.enabled=true",
    "quickpay.payment.cache.type=caffeine",
    "quickpay.payment.cache.ttl=1m",
    "logging.level.com.quickpay.payment.service=DEBUG"
})
class PaymentCacheTest {

    @Autowired
    private PaymentProcessor paymentProcessor;
    
    @Autowired
    private CacheManager cacheManager;
    
    @SpyBean
    private PaymentRepository paymentRepository;
    
    private PaymentId testPaymentId;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCache("payments").clear();
        
        // Reset spy interactions
        clearInvocations(paymentRepository);
        
        // Create test data
        testPaymentId = PaymentId.generate();
        testPayment = Payment.createPending(
            testPaymentId,
            CustomerId.of("cache-customer"),
            MerchantId.of("cache-merchant"),
            Money.usd(75.0)
        );
    }

    @Test
    void shouldCachePaymentLookupsAndAvoidRepositoryCallsOnSecondLookup() {
        // Given: Payment exists in repository after processing
        CompletableFuture<Payment> future = paymentProcessor.processPayment(testPayment);
        Payment processedPayment = future.join(); // Wait for completion
        
        // Clear spy invocations from processing
        clearInvocations(paymentRepository);

        // When: First lookup - should hit repository
        Optional<Payment> firstLookup = paymentProcessor.getPayment(testPaymentId);
        
        // Then: Should call repository once
        verify(paymentRepository, times(1)).findById(testPaymentId);
        assertThat(firstLookup).isPresent();
        assertThat(firstLookup.get().status()).isEqualTo(PaymentStatus.COMPLETED);

        // When: Second lookup - should hit cache
        Optional<Payment> secondLookup = paymentProcessor.getPayment(testPaymentId);
        
        // Then: Should NOT call repository again (still only 1 call total)
        verify(paymentRepository, times(1)).findById(testPaymentId);
        assertThat(secondLookup).isPresent();
        assertThat(secondLookup.get()).isEqualTo(firstLookup.get());
        
        // Verify cache contains the payment
        assertThat(cacheManager.getCache("payments").get(testPaymentId.value())).isNotNull();
    }

    @Test
    void shouldEvictCacheWhenPaymentIsProcessedAndForceRepositoryLookupAfterward() {
        // Given: Initial lookup that caches empty result
        Optional<Payment> emptyLookup = paymentProcessor.getPayment(testPaymentId);
        assertThat(emptyLookup).isEmpty();
        
        // Verify it was cached as empty
        verify(paymentRepository, times(1)).findById(testPaymentId);
        
        // Second lookup should hit cache (no additional repository call)
        Optional<Payment> cachedEmptyLookup = paymentProcessor.getPayment(testPaymentId);
        assertThat(cachedEmptyLookup).isEmpty();
        verify(paymentRepository, times(1)).findById(testPaymentId); // Still only 1 call

        // When: Process payment (this evicts cache)
        CompletableFuture<Payment> future = paymentProcessor.processPayment(testPayment);
        Payment processedPayment = future.join();
        
        // Then: Next lookup should hit repository again (cache was evicted)
        Optional<Payment> afterProcessing = paymentProcessor.getPayment(testPaymentId);
        
        // Verify repository was called again after cache eviction
        verify(paymentRepository, times(2)).findById(testPaymentId);
        assertThat(afterProcessing).isPresent();
        assertThat(afterProcessing.get().status()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void shouldEvictCacheOnRefundAndForceRepositoryLookup() throws Exception {
        // Given: Process and cache a payment
        CompletableFuture<Payment> future = paymentProcessor.processPayment(testPayment);
        future.join(); // Wait for completion
        
        // Cache the completed payment
        Optional<Payment> completedLookup = paymentProcessor.getPayment(testPaymentId);
        assertThat(completedLookup).isPresent();
        assertThat(completedLookup.get().status()).isEqualTo(PaymentStatus.COMPLETED);
        
        // Clear spy to focus on refund behavior
        clearInvocations(paymentRepository);

        // When: Refund payment (should evict cache)
        Payment refundedPayment = paymentProcessor.refundPayment(testPaymentId, "Cache test refund");
        assertThat(refundedPayment.status()).isEqualTo(PaymentStatus.REFUNDED);
        
        // Then: Next lookup should hit repository (cache evicted)
        Optional<Payment> afterRefund = paymentProcessor.getPayment(testPaymentId);
        
        // Verify repository was called for the fresh lookup
        verify(paymentRepository, times(1)).findById(testPaymentId);
        assertThat(afterRefund).isPresent();
        assertThat(afterRefund.get().status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(afterRefund.get().failureReason()).contains("Cache test refund");
    }

    @Test
    void shouldCacheEmptyResults() {
        // Given: Non-existent payment ID
        PaymentId nonExistentId = PaymentId.generate();

        // When: Multiple lookups for non-existent payment
        Optional<Payment> firstLookup = paymentProcessor.getPayment(nonExistentId);
        Optional<Payment> secondLookup = paymentProcessor.getPayment(nonExistentId);

        // Then: Both should be empty
        assertThat(firstLookup).isEmpty();
        assertThat(secondLookup).isEmpty();
        
        // Repository should only be called once (second call hits cache)
        verify(paymentRepository, times(1)).findById(nonExistentId);
        
        // Cache should contain the empty result
        assertThat(cacheManager.getCache("payments").get(nonExistentId.value())).isNotNull();
    }

    @Test
    void shouldDemonstrateKeyBasedCacheIsolation() throws Exception {
        // Given: Two different payments
        PaymentId payment1Id = PaymentId.generate();
        PaymentId payment2Id = PaymentId.generate();
        
        Payment payment1 = Payment.createPending(payment1Id, CustomerId.of("customer1"), 
                                               MerchantId.of("merchant1"), Money.usd(100.0));
        Payment payment2 = Payment.createPending(payment2Id, CustomerId.of("customer2"), 
                                               MerchantId.of("merchant2"), Money.eur(200.0));

        // Process both payments
        paymentProcessor.processPayment(payment1).join();
        paymentProcessor.processPayment(payment2).join();
        
        clearInvocations(paymentRepository);

        // When: Look up both payments
        Optional<Payment> lookup1 = paymentProcessor.getPayment(payment1Id);
        Optional<Payment> lookup2 = paymentProcessor.getPayment(payment2Id);

        // Then: Each should hit repository once and be cached separately
        verify(paymentRepository, times(1)).findById(payment1Id);
        verify(paymentRepository, times(1)).findById(payment2Id);
        
        assertThat(lookup1).isPresent();
        assertThat(lookup2).isPresent();
        assertThat(lookup1.get().amount().currency().getCurrencyCode()).isEqualTo("USD");
        assertThat(lookup2.get().amount().currency().getCurrencyCode()).isEqualTo("EUR");

        // Second lookups should hit cache
        paymentProcessor.getPayment(payment1Id);
        paymentProcessor.getPayment(payment2Id);
        
        // No additional repository calls
        verify(paymentRepository, times(1)).findById(payment1Id);
        verify(paymentRepository, times(1)).findById(payment2Id);
    }
}