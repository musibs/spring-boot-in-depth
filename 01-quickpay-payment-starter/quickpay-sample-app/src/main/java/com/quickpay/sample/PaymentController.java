package com.quickpay.sample;

import com.quickpay.payment.domain.*;
import com.quickpay.payment.service.PaymentProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private final PaymentProcessor paymentProcessor;
    
    public PaymentController(PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }
    
    @PostMapping
    public CompletableFuture<ResponseEntity<Payment>> createPayment(
            @RequestBody CreatePaymentRequest request) {
        
        Payment payment = Payment.createPending(
            PaymentId.generate(),
            CustomerId.of(request.customerId()),
            MerchantId.of(request.merchantId()),
            Money.of(request.amount(), Currency.getInstance(request.currency()))
        );
        
        return paymentProcessor.processPayment(payment)
            .thenApply(ResponseEntity::ok);
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId) {
        return paymentProcessor.getPayment(PaymentId.of(paymentId))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Payment> refundPayment(
            @PathVariable String paymentId,
            @RequestBody RefundRequest request) {
        try {
            Payment refundedPayment = paymentProcessor.refundPayment(
                PaymentId.of(paymentId), 
                request.reason()
            );
            return ResponseEntity.ok(refundedPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    public record CreatePaymentRequest(
        String customerId,
        String merchantId, 
        double amount,
        String currency
    ) {}
    
    public record RefundRequest(String reason) {}
}