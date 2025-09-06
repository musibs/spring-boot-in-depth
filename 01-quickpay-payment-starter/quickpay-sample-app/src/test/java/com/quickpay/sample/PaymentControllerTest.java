package com.quickpay.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickpay.payment.domain.*;
import com.quickpay.payment.service.PaymentProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Currency;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PaymentController REST endpoints.
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentProcessor paymentProcessor;

    @Test
    void shouldCreatePaymentSuccessfully() throws Exception {
        // Given: Payment request
        PaymentController.CreatePaymentRequest request = new PaymentController.CreatePaymentRequest(
            "customer-123", "merchant-456", 100.0, "USD"
        );

        // Mock processed payment
        Payment processedPayment = Payment.createPending(
            PaymentId.of("payment-789"),
            CustomerId.of("customer-123"),
            MerchantId.of("merchant-456"),
            Money.usd(100.0)
        ).markAsProcessing().markAsCompleted();

        when(paymentProcessor.processPayment(any(Payment.class)))
            .thenReturn(CompletableFuture.completedFuture(processedPayment));

        // When: POST payment creation
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                // Then: Should return processed payment
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId.value").value("payment-789"))
                .andExpect(jsonPath("$.customerId.value").value("customer-123"))
                .andExpect(jsonPath("$.merchantId.value").value("merchant-456"))
                .andExpect(jsonPath("$.amount.amount").value(100.0))
                .andExpect(jsonPath("$.amount.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldGetPaymentById() throws Exception {
        // Given: Existing payment
        PaymentId paymentId = PaymentId.of("payment-123");
        Payment existingPayment = Payment.createPending(
            paymentId,
            CustomerId.of("customer-456"),
            MerchantId.of("merchant-789"),
            Money.eur(250.0)
        ).markAsProcessing().markAsCompleted();

        when(paymentProcessor.getPayment(eq(paymentId)))
            .thenReturn(Optional.of(existingPayment));

        // When: GET payment by ID
        mockMvc.perform(get("/api/payments/payment-123"))
                
                // Then: Should return payment details
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId.value").value("payment-123"))
                .andExpect(jsonPath("$.customerId.value").value("customer-456"))
                .andExpect(jsonPath("$.merchantId.value").value("merchant-789"))
                .andExpect(jsonPath("$.amount.amount").value(250.0))
                .andExpect(jsonPath("$.amount.currency").value("EUR"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        // Given: Non-existent payment ID
        PaymentId paymentId = PaymentId.of("non-existent");
        when(paymentProcessor.getPayment(eq(paymentId)))
            .thenReturn(Optional.empty());

        // When: GET non-existent payment
        mockMvc.perform(get("/api/payments/non-existent"))
                
                // Then: Should return 404
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRefundPaymentSuccessfully() throws Exception {
        // Given: Refund request
        PaymentController.RefundRequest refundRequest = 
            new PaymentController.RefundRequest("Customer request");

        PaymentId paymentId = PaymentId.of("payment-refund");
        Payment refundedPayment = new Payment(
            paymentId,
            CustomerId.of("customer-123"),
            MerchantId.of("merchant-456"),
            Money.usd(100.0),
            PaymentStatus.REFUNDED,
            java.time.Instant.now().minusSeconds(3600),
            Optional.of(java.time.Instant.now()),
            Optional.of("Customer request")
        );

        when(paymentProcessor.refundPayment(eq(paymentId), eq("Customer request")))
            .thenReturn(refundedPayment);

        // When: POST refund request
        mockMvc.perform(post("/api/payments/payment-refund/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                
                // Then: Should return refunded payment
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId.value").value("payment-refund"))
                .andExpect(jsonPath("$.status").value("REFUNDED"))
                .andExpect(jsonPath("$.failureReason").value("Customer request"));
    }

    @Test
    void shouldReturn400WhenRefundFails() throws Exception {
        // Given: Refund request that will fail
        PaymentController.RefundRequest refundRequest = 
            new PaymentController.RefundRequest("Invalid refund");

        PaymentId paymentId = PaymentId.of("payment-fail");
        when(paymentProcessor.refundPayment(eq(paymentId), eq("Invalid refund")))
            .thenThrow(new IllegalStateException("Cannot refund non-completed payment"));

        // When: POST refund request
        mockMvc.perform(post("/api/payments/payment-fail/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                
                // Then: Should return 400
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleInvalidPaymentRequest() throws Exception {
        // Given: Invalid payment request (negative amount)
        PaymentController.CreatePaymentRequest invalidRequest = 
            new PaymentController.CreatePaymentRequest(
                "customer-123", "merchant-456", -100.0, "USD"
            );

        // When payment creation fails due to validation
        when(paymentProcessor.processPayment(any(Payment.class)))
            .thenThrow(new IllegalArgumentException("Payment amount must be positive"));

        // When: POST invalid payment
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                
                // Then: Should return error (in this case, the controller doesn't handle it gracefully)
                // In a real implementation, you'd want proper error handling
                .andExpect(status().is5xxServerError());
    }
}