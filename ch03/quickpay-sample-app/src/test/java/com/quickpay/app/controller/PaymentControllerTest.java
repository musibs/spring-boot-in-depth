package com.quickpay.app.controller;

import com.quickpay.app.model.PaymentRequest;
import com.quickpay.app.model.PaymentResponse;
import com.quickpay.app.service.PaymentProcessingService;
import com.quickpay.observability.events.MetricEventPublisher;
import com.quickpay.observability.metrics.PaymentMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    private PaymentController paymentController;
    private PaymentProcessingService paymentProcessingService;
    private PaymentMetrics paymentMetrics;
    private MetricEventPublisher metricEventPublisher;

    @BeforeEach
    void setUp() {
        paymentProcessingService = mock(PaymentProcessingService.class);
        paymentMetrics = mock(PaymentMetrics.class);
        metricEventPublisher = mock(MetricEventPublisher.class);
        
        paymentController = new PaymentController(
            paymentProcessingService,
            paymentMetrics,
            metricEventPublisher
        );
    }

    @Test
    void createPayment_Success() {
        // Arrange
        var paymentRequest = new PaymentRequest(
            100.50,
            "USD",
            "cust_123",
            "merch_456",
            "Test payment"
        );
        
        var mockResponse = new PaymentResponse(
            "txn_123",
            "SUCCESS",
            "Payment processed successfully",
            100.50,
            "USD",
            Instant.now()
        );
        
        when(paymentProcessingService.processPayment(any(PaymentRequest.class), anyString()))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<PaymentResponse> result = paymentController.createPayment(paymentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("SUCCESS", result.getBody().status());
        assertEquals(100.50, result.getBody().amount());
        assertEquals("USD", result.getBody().currency());
        assertEquals("txn_123", result.getBody().transactionId());
        
        // Verify interactions
        verify(paymentProcessingService).processPayment(eq(paymentRequest), anyString());
        verify(metricEventPublisher).publishPaymentProcessed(anyString(), any(), anyString(), anyString(), anyDouble());
        verify(paymentMetrics).incrementPaymentCounter("sample-provider", "USD");
    }

    @Test
    void createPayment_LargeAmount_Pending() {
        // Arrange
        var paymentRequest = new PaymentRequest(
            15000.00,
            "USD",
            "cust_123",
            "merch_456",
            "Large payment test"
        );
        
        var mockResponse = new PaymentResponse(
            "txn_456",
            "PENDING",
            "Large payment requires additional verification",
            15000.00,
            "USD",
            Instant.now()
        );
        
        when(paymentProcessingService.processPayment(any(PaymentRequest.class), anyString()))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<PaymentResponse> result = paymentController.createPayment(paymentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(400, result.getStatusCode().value());  // Controller returns badRequest for non-SUCCESS status
        assertNotNull(result.getBody());
        assertEquals("PENDING", result.getBody().status());
    }

    @Test
    void getPayment_NotFound() {
        // Arrange
        when(paymentProcessingService.getPayment("nonexistent_txn_123"))
            .thenReturn(null);

        // Act
        ResponseEntity<PaymentResponse> result = paymentController.getPayment("nonexistent_txn_123");

        // Assert
        assertNotNull(result);
        assertEquals(404, result.getStatusCode().value());
        assertNull(result.getBody());
        
        // Verify interactions
        verify(paymentProcessingService).getPayment("nonexistent_txn_123");
        verify(metricEventPublisher).publishLatencyMetric(eq("payment_lookup"), any(java.time.Duration.class), eq("sample-provider"), eq(true));
    }

    @Test
    void getPayment_Found() {
        // Arrange
        var mockResponse = new PaymentResponse(
            "txn_789",
            "SUCCESS",
            "Payment processed successfully",
            50.00,
            "USD",
            Instant.now()
        );
        
        when(paymentProcessingService.getPayment("txn_789"))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<PaymentResponse> result = paymentController.getPayment("txn_789");

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("SUCCESS", result.getBody().status());
        assertEquals("txn_789", result.getBody().transactionId());
        
        // Verify interactions
        verify(paymentProcessingService).getPayment("txn_789");
        verify(metricEventPublisher).publishLatencyMetric(eq("payment_lookup"), any(java.time.Duration.class), eq("sample-provider"), eq(true));
    }
}