package com.quickpay.app.service;

//import com.quickpay.logging.correlation.TransactionContextHolder;
import com.quickpay.app.model.PaymentRequest;
import com.quickpay.app.model.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final Map<String, PaymentResponse> paymentStorage = new ConcurrentHashMap<>();
    
    public PaymentResponse processPayment(PaymentRequest request, String transactionId) {
        logger.info("Starting payment processing for transaction: {}", transactionId);
        
        //var currentTransactionId = TransactionContextHolder.getCurrentTransactionId();
        //logger.debug("Current transaction ID from context: {}", currentTransactionId);
        
        try {
            var response = simulatePaymentProcessing(request, transactionId);
            paymentStorage.put(transactionId, response);
            logger.info("Payment processing completed for transaction: {} with status: {}", 
                       transactionId, response.status());
            return response;
            
        } catch (Exception e) {
            
            return new PaymentResponse(
                transactionId,
                "FAILED",
                "Processing was interrupted",
                request.amount(),
                request.currency(),
                Instant.now()
            );
        }
    }
    
    public PaymentResponse getPayment(String transactionId) {
        logger.info("Retrieving payment for transaction: {}", transactionId);
        
        var payment = paymentStorage.get(transactionId);
        
        if (payment == null) {
            logger.warn("Payment not found for transaction: {}", transactionId);
        } else {
            logger.info("Found payment for transaction: {} with status: {}", 
                       transactionId, payment.status());
        }
        
        return payment;
    }
    
    private PaymentResponse simulatePaymentProcessing(PaymentRequest request, String transactionId) {
        // Simulate different outcomes based on amount
        if (request.amount() > 10000) {
            logger.warn("Large amount payment requires additional verification: {}", request.amount());
            return new PaymentResponse(
                transactionId,
                "PENDING",
                "Large payment requires additional verification",
                request.amount(),
                request.currency(),
                Instant.now()
            );
        } else if (request.amount() < 0.01) {
            logger.error("Invalid payment amount: {}", request.amount());
            return new PaymentResponse(
                transactionId,
                "FAILED",
                "Invalid payment amount",
                request.amount(),
                request.currency(),
                Instant.now()
            );
        } else {
            logger.info("Payment approved for amount: {} {}", request.amount(), request.currency());
            return new PaymentResponse(
                transactionId,
                "SUCCESS",
                "Payment processed successfully",
                request.amount(),
                request.currency(),
                Instant.now()
            );
        }
    }
}