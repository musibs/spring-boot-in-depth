package com.quickpay.logging.correlation;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Thread-safe transaction context record that holds correlation information
 * for distributed tracing and logging correlation across QuickPay services.
 *
 * This record is immutable and provides a clean API for transaction correlation
 * with automatic validation and defensive copying.
 *
 * @param transactionId the unique transaction identifier
 * @param userId the user identifier (nullable)
 * @param serviceId the service identifier
 * @param timestamp the transaction timestamp
 * @param additionalContext additional context information
 */
public record TransactionContext(
        String transactionId,
        String userId,
        String serviceId,
        Instant timestamp,
        Map<String, String> additionalContext
) {
    
    /**
     * Compact constructor with validation and defensive copying.
     */
    public TransactionContext {
        Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        Objects.requireNonNull(serviceId, "Service ID cannot be null");
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        
        if (transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be empty");
        }
        if (serviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Service ID cannot be empty");
        }
        
        // Defensive copy of additional context
        additionalContext = additionalContext != null ? Map.copyOf(additionalContext) : Map.of();
    }
    
    /**
     * Factory method to create a new transaction context with generated transaction ID.
     * 
     * @param serviceId the service identifier
     * @return new TransactionContext with generated transaction ID
     */
    public static TransactionContext create(String serviceId) {
        return new TransactionContext(
            TransactionIdGenerator.generate(),
            null,
            serviceId,
            Instant.now(),
            Map.of()
        );
    }
    
    /**
     * Create a transaction context with a specific transaction ID.
     * 
     * @param transactionId the specific transaction ID to use
     * @param serviceId the service identifier
     * @return new TransactionContext with provided transaction ID
     */
    public static TransactionContext of(String transactionId, String serviceId) {
        return new TransactionContext(
            transactionId,
            null,
            serviceId,
            Instant.now(),
            Map.of()
        );
    }
    
    /**
     * Create a new TransactionContext with a user ID.
     * 
     * @param userId the user identifier
     * @return new TransactionContext with user ID set
     */
    public TransactionContext withUser(String userId) {
        return new TransactionContext(transactionId, userId, serviceId, timestamp, additionalContext);
    }
    
    /**
     * Create a new TransactionContext with additional context information.
     * 
     * @param key the context key
     * @param value the context value
     * @return new TransactionContext with additional context
     */
    public TransactionContext withContext(String key, String value) {
        Objects.requireNonNull(key, "Context key cannot be null");
        Objects.requireNonNull(value, "Context value cannot be null");
        
        var newContext = new java.util.HashMap<>(additionalContext);
        newContext.put(key, value);
        var immutableContext = Map.copyOf(newContext);
            
        return new TransactionContext(transactionId, userId, serviceId, timestamp, immutableContext);
    }
    
    /**
     * Get the user ID if present.
     * 
     * @return Optional containing user ID if set
     */
    public Optional<String> user() {
        return Optional.ofNullable(userId);
    }
    
    /**
     * Get additional context value by key.
     * 
     * @param key the context key
     * @return Optional containing context value if present
     */
    public Optional<String> getContext(String key) {
        return Optional.ofNullable(additionalContext.get(key));
    }
}