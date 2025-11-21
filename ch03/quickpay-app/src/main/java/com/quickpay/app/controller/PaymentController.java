package com.quickpay.app.controller;

import com.quickpay.logging.domain.Money;
import com.quickpay.observability.events.MetricEventPublisher;
import com.quickpay.observability.metrics.PaymentMetrics;
import com.quickpay.app.model.PaymentRequest;
import com.quickpay.app.model.PaymentResponse;
import com.quickpay.app.service.PaymentProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentProcessingService paymentProcessingService;
    private final PaymentMetrics paymentMetrics;
    private final MetricEventPublisher metricEventPublisher;

    public PaymentController(PaymentProcessingService paymentProcessingService,
                             PaymentMetrics paymentMetrics,
                             MetricEventPublisher metricEventPublisher) {
        this.paymentProcessingService = paymentProcessingService;
        this.paymentMetrics = paymentMetrics;
        this.metricEventPublisher = metricEventPublisher;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        var startTime = Instant.now();
        var transactionId = "txn_" + UUID.randomUUID().toString().replace("-", "");

        logger.info("Processing payment request for money: {} {}", request.money(), request.money().currency());

        try {
            // Process the payment using the service
            var response = paymentProcessingService.processPayment(request, transactionId);

            // Record metrics using the observability starter
            var processingDuration = Duration.between(startTime, Instant.now());

            if (response.status().equals("SUCCESS")) {
                // Record successful payment
                metricEventPublisher.publishPaymentProcessed(
                        transactionId,
                        processingDuration,
                        "sample-provider",
                        request.money().getCurrencyCode(),
                        request.money().amount().doubleValue()
                );

                paymentMetrics.incrementPaymentCounter("sample-provider", request.money().getCurrencyCode());
                paymentMetrics.recordProcessingTime(processingDuration, "sample-provider", request.money().getCurrencyCode());

                logger.info("Payment processed successfully with transaction ID: {}", transactionId);
                return ResponseEntity.ok(response);
            } else {
                // Record failed payment
                metricEventPublisher.publishPaymentFailed(
                        transactionId,
                        processingDuration,
                        "sample-provider",
                        "PROCESSING_ERROR",
                        "Payment processing failed"
                );

                logger.error("Payment processing failed for transaction ID: {}", transactionId);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            var processingDuration = Duration.between(startTime, Instant.now());

            metricEventPublisher.publishPaymentFailed(
                    transactionId,
                    processingDuration,
                    "sample-provider",
                    "SYSTEM_ERROR",
                    e.getMessage()
            );

            logger.error("Exception occurred while processing payment: ", e);

            var errorResponse = new PaymentResponse(
                    transactionId,
                    "FAILED",
                    "System error occurred",
                    Money.of("0.00", "USD"),
                    Instant.now()
            );

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String transactionId) {
        logger.info("Retrieving payment information for transaction ID: {}", transactionId);

        // Record latency metric
        var startTime = Instant.now();

        try {
            var response = paymentProcessingService.getPayment(transactionId);

            var latency = Duration.between(startTime, Instant.now());
            metricEventPublisher.publishLatencyMetric("payment_lookup",
                    latency, "sample-provider", true);

            if (response != null) {
                logger.info("Found payment for transaction ID: {}", transactionId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Payment not found for transaction ID: {}", transactionId);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            var latency = Duration.between(startTime, Instant.now());
            metricEventPublisher.publishLatencyMetric("payment_lookup", latency, "sample-provider", false);

            logger.error("Error retrieving payment: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}