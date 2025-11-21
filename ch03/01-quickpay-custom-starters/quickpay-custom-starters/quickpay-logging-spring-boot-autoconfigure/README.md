# QuickPay Logging Spring Boot Starter

A Spring Boot auto-configuration starter that provides ECS-compliant structured logging with transaction correlation for QuickPay applications.

## Features

### ✅ Infrastructure-Focused Design
- **Pure logging infrastructure** - No business domain objects
- **ECS-compliant JSON logging** - Elastic Common Schema v8.11 support (enforced, non-overrideable)
- **Transaction correlation** - Distributed tracing across microservices
- **Security-first approach** - PII masking and data sanitization
- **Automatic ECS enforcement** - Zero configuration, ECS format cannot be disabled

### 🔧 Core Components

1. **QuickPayEcsFormatter** - ECS-compliant structured log formatter
2. **TransactionContext** - Thread-safe correlation context
3. **TransactionIdFilter** - HTTP request correlation management
4. **Auto-configuration** - Zero-config Spring Boot integration

## Quick Start

### 1. Add Dependency

```gradle
dependencies {
    implementation 'com.quickpay:quickpay-logging-spring-boot-starter:1.0.0'
}
```

### 2. Configure Application Properties

```yaml
quickpay:
  logging:
    enabled: true  # Master switch - when true, ECS format is ALWAYS enforced
    ecs:
      pii-masking: true  # Only configurable ECS option
    correlation:
      enabled: true
      header-name: "X-Transaction-ID"
      generate-if-missing: true
      add-to-response: true
    service:
      name: "payment-service"
      version: "1.0.0"
      environment: "production"

# 🔒 ECS format is AUTOMATICALLY and UNCONDITIONALLY enforced!
# No manual configuration needed or possible!
```

### 3. Enable Enhanced Features with @EnableQuickPayLogging (Optional)

```java
@EnableQuickPayLogging(
    serviceName = "payment-service",
    environment = "production",
    enableAsyncLogging = true,      // High-performance async logging
    auditingEnabled = true,         // Compliance audit trails
    performanceMonitoring = true,   // SLA tracking
    correlationHeaders = {"X-Request-ID", "X-Tenant-ID"},
    sensitiveFields = {"accountNumber", "routingNumber"}
)
@SpringBootApplication
public class PaymentServiceApplication {
}
```

### 4. Use Transaction Context (Optional)

```java
@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    public void processPayment(String paymentId, String userId) {
        // Create transaction context
        TransactionContext context = TransactionContext.create("payment-service")
            .withUser(userId)
            .withContext("paymentId", paymentId);
            
        // Execute with context (automatically manages MDC)
        TransactionContextHolder.executeWithContext(context, () -> {
            logger.info("Processing payment: {}", paymentId);
            // Your business logic here
        });
    }
}
```

## @EnableQuickPayLogging Features

### 🔒 **Core Principle**
- **ECS format**: ALWAYS enforced (automatic, non-overrideable)
- **Enhanced features**: Enabled by annotation (optional)

### 🚀 **Available Features**

| Feature | Purpose | Use Case |
|---------|---------|----------|
| `enableAsyncLogging` | High-performance non-blocking logging | Services >10K TPS |
| `auditingEnabled` | PCI-DSS/SOX compliant audit trails | Financial services |
| `performanceMonitoring` | SLA tracking with p95/p99 metrics | Performance-critical services |
| `errorCategorizationEnabled` | AI-powered error analysis | Complex distributed systems |
| `metricsEnabled` | Business KPI collection | Analytics and reporting |
| `correlationHeaders` | Multi-system request tracing | Multi-tenant architectures |
| `sensitiveFields` | Enhanced PII masking | Compliance-heavy applications |
| `customTags` | Service categorization | Operations and filtering |

### 📚 **Quick Examples**

**High-Volume Service:**
```java
@EnableQuickPayLogging(
    enableAsyncLogging = true,
    performanceMonitoring = true,
    metricsEnabled = true
)
```

**Compliance Service:**
```java
@EnableQuickPayLogging(
    auditingEnabled = true,
    sensitiveFields = {"ssn", "creditCard"},
    customTags = {"compliance=pci-dss"}
)
```

**Multi-Tenant Service:**
```java
@EnableQuickPayLogging(
    correlationHeaders = {"X-Tenant-ID", "X-Partner-ID"},
    customTags = {"service-tier=premium"}
)
```

## Configuration Reference

### Main Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `quickpay.logging.enabled` | `true` | Master switch for QuickPay logging |

### ECS Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `quickpay.logging.ecs.pii-masking` | `true` | Automatically mask PII data |

**Note**: There is NO `quickpay.logging.ecs.enabled` property. ECS format is ALWAYS active when QuickPay logging is enabled.

### Correlation Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `quickpay.logging.correlation.enabled` | `true` | Enable transaction correlation |
| `quickpay.logging.correlation.header-name` | `X-Transaction-ID` | HTTP header for transaction ID |
| `quickpay.logging.correlation.generate-if-missing` | `true` | Generate ID if not in request |
| `quickpay.logging.correlation.add-to-response` | `true` | Add transaction ID to response |

### Service Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `quickpay.logging.service.name` | `quickpay-service` | Service name for logs |
| `quickpay.logging.service.version` | `1.0.0` | Service version |
| `quickpay.logging.service.environment` | `development` | Deployment environment |

## Automatic ECS Enforcement

**🔒 Key Feature: ECS format is UNCONDITIONALLY enforced when QuickPay logging is enabled.**

- **Console logging**: ALWAYS ECS JSON format (cannot be changed)
- **File logging**: ALWAYS ECS JSON format (cannot be changed)
- **Cannot be overridden**: Maximum precedence property enforcement
- **Cannot be disabled**: No `quickpay.logging.ecs.enabled` property exists
- **Zero configuration**: No manual `logging.structured.format` configuration possible

## ECS Log Format

The starter automatically produces ECS-compliant JSON logs with the following structure:

```json
{
  "@timestamp": "2024-01-15T10:30:45.123Z",
  "ecs": {
    "version": "8.11"
  },
  "service": {
    "name": "payment-service",
    "version": "1.0.0",
    "environment": "production"
  },
  "host": {
    "hostname": "payment-pod-123",
    "ip": "10.0.1.42"
  },
  "transaction": {
    "id": "txn_1705315845123_AbCdEf123456"
  },
  "user": {
    "id": "user-789"
  },
  "log": {
    "level": "INFO",
    "logger": "com.example.PaymentService"
  },
  "message": "Payment processed successfully",
  "labels": {
    "paymentId": "pay-123",
    "region": "us-west-2"
  }
}
```

## Transaction Correlation

### Automatic HTTP Correlation

The starter automatically:

1. **Extracts** transaction IDs from HTTP headers:
   - `X-Transaction-ID` (configurable)
   - `X-Correlation-ID` (fallback)
   - `X-Trace-ID` (fallback)

2. **Generates** new transaction IDs if missing (when enabled)

3. **Propagates** context through the request lifecycle

4. **Adds** transaction ID to response headers (when enabled)

5. **Updates** MDC for logging integration

### Manual Context Management

```java
// Create and set context
TransactionContext context = TransactionContext.create("my-service")
    .withUser("user-123")
    .withContext("operation", "payment-processing");
    
TransactionContextHolder.setContext(context);

try {
    // Your code here - all logging will include transaction context
    logger.info("Processing operation");
} finally {
    TransactionContextHolder.clear(); // Important: always clean up
}
```

## Security Features

### PII Masking

When `pii-masking` is enabled (default: `true`), the formatter automatically masks sensitive data:

```java
// Input log data
Map<String, Object> logData = Map.of(
    "message", "User login",
    "email", "user@example.com",
    "password", "secret123",
    "cardNumber", "4111111111111111"
);

// Output (masked)
{
    "message": "User login",
    "email": "us***@example.com",
    "password": "***",
    "cardNumber": "41***********11"
}
```

Masked fields include: `password`, `token`, `secret`, `key`, `credential`, `authorization`, `card`, `account`, `ssn`, `email`, `phone`.

## Production Configuration

```yaml
# Recommended production settings
quickpay:
  logging:
    enabled: true  # 🔒 When true, ECS format is UNCONDITIONALLY enforced
    ecs:
      pii-masking: true  # 🔒 ALWAYS enable in production for security
    correlation:
      enabled: true
      header-name: "X-Transaction-ID"
      generate-if-missing: true
      add-to-response: false  # Don't expose internal transaction IDs
    service:
      name: "${spring.application.name}"
      version: "${app.version:1.0.0}"
      environment: "${spring.profiles.active:production}"

# 🔒 ECS format is UNCONDITIONALLY enforced - no configuration needed or possible!
# Optional: Set log levels if needed
logging:
  level:
    com.quickpay.logging: INFO
```

## Testing

The starter includes comprehensive auto-configuration tests:

```bash
./gradlew test
```

### Test Configuration

```java
@SpringBootTest
@TestPropertySource(properties = {
    "quickpay.logging.enabled=true",
    "quickpay.logging.service.name=test-service"
})
class MyServiceTest {
    
    @Test
    void shouldLogWithTransactionContext() {
        TransactionContext context = TransactionContext.create("test-service");
        
        TransactionContextHolder.executeWithContext(context, () -> {
            logger.info("Test log message");
            // Verify transaction ID is in MDC
            assertEquals(context.transactionId(), 
                        MDC.get(TransactionContextHolder.TRANSACTION_ID_KEY));
        });
    }
}
```

## Architecture Decisions

### ✅ What This Starter Does (Infrastructure)
- **UNCONDITIONALLY enforced ECS logging** (absolutely cannot be disabled or overridden)
- Transaction correlation across services
- HTTP request/response correlation
- PII masking and security
- Service identification in logs
- Maximum-precedence property enforcement

### ❌ What This Starter Doesn't Do (Business Logic)
- Payment domain objects
- Business-specific formatters
- Application-specific logging logic
- Domain event handling

### Design Principles

1. **Separation of Concerns** - Pure infrastructure, no business logic
2. **Security by Default** - PII masking enabled by default
3. **Zero Configuration** - Works out of the box with sensible defaults
4. **Spring Boot Native** - Leverages Spring Boot's structured logging
5. **Thread Safety** - All components are thread-safe
6. **Performance Optimized** - Minimal overhead, efficient JSON serialization

## @EnableQuickPayLogging vs Properties

### **Annotation Approach (Recommended)**
```java
@EnableQuickPayLogging(
    serviceName = "payment-service",
    environment = "production",
    enableAsyncLogging = true
)
@SpringBootApplication
public class PaymentServiceApplication {}
```

### **Properties Approach**
```yaml
quickpay:
  logging:
    service:
      name: "payment-service"
      environment: "production"
```

### **Why Use the Annotation?**
- ✅ **Clear declaration** of service logging requirements
- ✅ **Enhanced features** not available via properties
- ✅ **Type-safe configuration** with IDE support
- ✅ **Self-documenting** service capabilities
- ✅ **Centralized logging setup** in one place

## Migration from Previous Version

If migrating from the old payment-specific logging:

1. **Remove payment domain dependencies** - Move `PaymentSuccessEvent`, `PaymentFailedEvent` to your payment-core-starter
2. **Update configuration** - Remove `quickpay.logging.ecs.enabled` (no longer exists)
3. **Replace PaymentLogger** - Use standard SLF4J loggers with transaction context
4. **Remove ALL manual logging configuration** - Delete any `logging.structured.format` properties (enforced automatically)
5. **Add @EnableQuickPayLogging** - Unlock enhanced features and clear service documentation
6. **Accept ECS enforcement** - ECS format is now non-negotiable

## Troubleshooting

### Common Issues

1. **Logs not in ECS format**
   - This should NEVER happen - ECS format is unconditionally enforced
   - Verify `quickpay.logging.enabled=true` (only master switch that matters)
   - Check startup logs for "🔒 ENFORCED: ECS logging format is now active" message
   - If still not ECS: Check if starter is on classpath and properly configured

2. **Missing transaction IDs**
   - Check `quickpay.logging.correlation.enabled=true`
   - Verify HTTP headers are being sent
   - Ensure filter is registered (web application context required)

3. **Configuration not loading**
   - Verify starter is on classpath
   - Check `quickpay.logging.enabled=true`
   - Ensure proper YAML indentation

### Debug Logging

```yaml
logging:
  level:
    com.quickpay.logging: DEBUG
```

## Contributing

When contributing to this starter:

1. **Keep it focused** - Only infrastructure concerns
2. **Maintain thread safety** - All components must be thread-safe
3. **Add tests** - Comprehensive test coverage required
4. **Follow ECS standards** - Stick to ECS specification
5. **Document changes** - Update README and configuration metadata

## License

Copyright © 2024 QuickPay. All rights reserved.