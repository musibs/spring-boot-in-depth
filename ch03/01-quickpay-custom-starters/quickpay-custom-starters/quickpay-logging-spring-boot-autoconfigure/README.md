# QuickPay Logging Spring Boot Auto-Configuration

This module provides auto-configuration for standardized payment logging with ECS format and comprehensive PII masking capabilities for QuickPay services.

## Features

- **ECS Compliance**: Elastic Common Schema compliant structured logging
- **PII Masking**: Comprehensive field-level masking with extensible strategies
- **Type-Safe Logging**: Dedicated `PaymentLogger` service for payment events
- **Auto-Detection**: Automatic PII detection using configurable patterns
- **Governance**: Locked-down configuration to ensure compliance
- **Modern Java**: Built with Java 24 features (records, sealed interfaces, pattern matching)

## Core Components

### Domain Events
- `PaymentSuccessEvent` - Immutable record for successful payments
- `PaymentFailedEvent` - Immutable record for failed payments
- `Money`, `PaymentMethod`, `FailureReason` - Supporting domain types

### PII Masking
- `MaskingStrategy` - Functional interface with predefined strategies
- `FieldMaskingService` - Dynamic field masking with auto-detection
- `MaskingConfiguration` - Type-safe configuration for masking rules
- `@SensitiveData` - Annotation for marking sensitive fields

### Logging Infrastructure
- `PaymentStructuredLoggingEncoder` - ECS-compliant log encoder using Spring Boot's structured logging with PII masking
- `PaymentLogger` - Type-safe payment event logging service
- Integration with Spring Boot's structured logging framework

## Usage

### Basic Configuration

```yaml
quickpay:
  logging:
    enabled: true
    ecs:
      enabled: true
      strictMode: true
    masking:
      enabled: true
      mode: STRICT
    service:
      name: "my-payment-service"
      version: "1.0.0"
      environment: "production"
```

### Using PaymentLogger

```java
@Service
public class PaymentService {
    
    private final PaymentLogger paymentLogger;
    
    public PaymentService(PaymentLogger paymentLogger) {
        this.paymentLogger = paymentLogger;
    }
    
    public void processPayment(PaymentRequest request) {
        paymentLogger.logProcessingStarted(request.transactionId(), 
                                          request.merchantId(), 
                                          request.correlationId());
        
        try {
            // Process payment...
            
            PaymentSuccessEvent event = PaymentSuccessEvent.create(
                request.transactionId(), request.merchantId(), request.amount(),
                authCode, PaymentMethod.CREDIT_CARD, processingTime, 
                request.correlationId()
            );
            
            paymentLogger.logSuccess(event);
            
        } catch (PaymentException e) {
            PaymentFailedEvent event = PaymentFailedEvent.create(
                request.transactionId(), request.merchantId(), request.amount(),
                e.getErrorCode(), e.getMessage(), 0, e.getFailureReason(),
                request.correlationId()
            );
            
            paymentLogger.logFailure(event);
        }
    }
}
```

### Annotation-Based Configuration

```java
@Configuration
@EnableQuickPayLogging(
    strictEcs = true,
    maskingMode = MaskingMode.AUTO_DETECT,
    auditEnabled = true
)
public class PaymentServiceConfiguration {
    // Additional configuration
}
```

## Masking Configuration

### Predefined Strategies

- `FULL_MASK` - Masks entire value with asterisks
- `PARTIAL_MASK` - Shows last 4 characters
- `CREDIT_CARD` - Credit card number masking (preserves format)
- `EMAIL` - Masks local part, preserves domain
- `SSN` - Social Security Number masking
- `PHONE` - Phone number masking (keeps area code and last 4 digits)

### Custom Field Rules

```yaml
quickpay:
  logging:
    masking:
      enabled: true
      mode: STRICT
      customStrategies:
        "customer.taxId": "PARTIAL_MASK"
        "payment.routingNumber": "FIRST_HALF"
```

### Auto-Detection Patterns

The starter includes built-in patterns for:
- Credit card numbers
- Email addresses
- Phone numbers
- Social Security Numbers
- IP addresses
- UUIDs

## ECS Field Mapping

All payment events are automatically mapped to ECS standard fields:

```json
{
  "@timestamp": "2024-01-15T10:30:45.123Z",
  "ecs.version": "8.11.0",
  "event": {
    "category": "payment",
    "type": "success",
    "action": "processed",
    "outcome": "success"
  },
  "payment": {
    "transaction_id": "TXN-123",
    "merchant_id": "MERCHANT-456",
    "amount": "100.00",
    "currency": "USD",
    "authorization_code": "AUTH-***6789",
    "payment_method": "CREDIT_CARD"
  },
  "service": {
    "name": "my-payment-service",
    "version": "1.0.0",
    "environment": "production"
  }
}
```

## Testing

The starter includes comprehensive test coverage:

```bash
./gradlew test
```

## Health Monitoring

When Spring Boot Actuator is present, a health indicator is automatically registered:

```bash
curl http://localhost:8080/actuator/health/paymentLogging
```

## Security & Compliance

- **Immutable Configuration**: Records prevent runtime modification
- **Fail-Safe Masking**: Never fails logging due to masking errors
- **Audit Trail**: Optional audit logging for compliance
- **PCI-DSS Ready**: Built-in rules for payment card data