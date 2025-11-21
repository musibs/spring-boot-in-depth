package com.quickpay.app.service;

import com.quickpay.app.model.PaymentRequest;
import com.quickpay.app.model.PaymentResponse;
import com.quickpay.logging.domain.FailureReason;
import com.quickpay.logging.domain.PaymentFailedEvent;
import com.quickpay.logging.domain.PaymentSuccessEvent;
import com.quickpay.logging.service.PaymentLogger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentProcessingService {

    private final PaymentLogger paymentLogger;
    private final Map<String, PaymentResponse> paymentStorage = new ConcurrentHashMap<>();

    public PaymentProcessingService(PaymentLogger paymentLogger) {
        this.paymentLogger = paymentLogger;
    }

    public PaymentResponse processPayment(PaymentRequest request, String transactionId) {
        Instant start = Instant.now();
        try {

            var response = simulatePaymentProcessing(request, transactionId);
            paymentStorage.put(transactionId, response);
            Instant end = Instant.now();
            paymentLogger.logSuccess(PaymentSuccessEvent.create(transactionId,
                    request.customerId(),
                    request.money(),
                    request.paymentDescription(),
                    request.paymentMethod(),
                    "AUTH_CODE_12345", // Simulated authorization code
                    Duration.between(start, end)
            ));
            return response;

        } catch (Exception e) {
            paymentLogger.logFailure(PaymentFailedEvent.create(transactionId,
                    request.customerId(),
                    request.money(),
                    request.paymentDescription(),
                    request.paymentMethod(),
                    "QUICK-PAY-001",
                    "Transaction failed due to processing error",
                    1,
                    FailureReason.EXPIRED_CARD
            ));
            return new PaymentResponse(
                    transactionId,
                    "FAILED",
                    "Processing was failed: " + e.getMessage(),
                    request.money(),
                    Instant.now()
            );
        }
    }

    public PaymentResponse getPayment(String transactionId) {
        var payment = paymentStorage.get(transactionId);
        return payment;
    }

    private PaymentResponse simulatePaymentProcessing(PaymentRequest request, String transactionId) {
        // Simulate different outcomes based on money
        if (request.isHighValue()) {
            return new PaymentResponse(
                    transactionId,
                    "PENDING",
                    "Large payment requires additional verification",
                    request.money(),
                    Instant.now()
            );
        } else if (request.isNegativeValue()) {
            return new PaymentResponse(
                    transactionId,
                    "FAILED",
                    "Invalid payment money",
                    request.money(),
                    Instant.now()
            );
        } else {
            return new PaymentResponse(
                    transactionId,
                    "SUCCESS",
                    "Payment processed successfully",
                    request.money(),
                    Instant.now()
            );
        }
    }
}