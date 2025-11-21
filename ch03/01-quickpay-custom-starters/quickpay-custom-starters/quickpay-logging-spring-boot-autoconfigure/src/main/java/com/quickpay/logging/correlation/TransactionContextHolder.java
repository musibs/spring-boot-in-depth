package com.quickpay.logging.correlation;

import org.slf4j.MDC;

import java.util.Optional;

/**
 * Thread-local holder for transaction context with automatic MDC integration.
 * 
 * This class provides a clean API for managing transaction context across
 * thread boundaries while automatically keeping MDC synchronized for 
 * structured logging integration.
 */
public final class TransactionContextHolder {
    
    // MDC keys for ECS compliance
    public static final String TRANSACTION_ID_KEY = "transaction.id";
    public static final String USER_ID_KEY = "user.id";
    public static final String SERVICE_ID_KEY = "service.id";
    
    private static final ThreadLocal<TransactionContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    // Private constructor to prevent instantiation
    private TransactionContextHolder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Set the transaction context for the current thread.
     * Automatically updates MDC with correlation information.
     * 
     * @param context the transaction context to set
     */
    public static void setContext(TransactionContext context) {
        if (context == null) {
            clear();
            return;
        }
        
        CONTEXT_HOLDER.set(context);
        updateMdc(context);
    }
    
    /**
     * Get the current transaction context.
     * 
     * @return Optional containing the current context if set
     */
    public static Optional<TransactionContext> getContext() {
        return Optional.ofNullable(CONTEXT_HOLDER.get());
    }
    
    /**
     * Get the current transaction ID.
     * 
     * @return Optional containing the current transaction ID if context is set
     */
    public static Optional<String> getTransactionId() {
        return getContext().map(TransactionContext::transactionId);
    }
    
    /**
     * Get the current user ID.
     * 
     * @return Optional containing the current user ID if context is set and user is present
     */
    public static Optional<String> getUserId() {
        return getContext().flatMap(TransactionContext::user);
    }
    
    /**
     * Get the current service ID.
     * 
     * @return Optional containing the current service ID if context is set
     */
    public static Optional<String> getServiceId() {
        return getContext().map(TransactionContext::serviceId);
    }
    
    /**
     * Clear the transaction context for the current thread.
     * Also clears related MDC entries.
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
        clearMdc();
    }
    
    /**
     * Execute a runnable with a specific transaction context.
     * The context is automatically restored after execution.
     * 
     * @param context the context to use during execution
     * @param runnable the code to execute
     */
    public static void executeWithContext(TransactionContext context, Runnable runnable) {
        TransactionContext previousContext = CONTEXT_HOLDER.get();
        try {
            setContext(context);
            runnable.run();
        } finally {
            if (previousContext != null) {
                setContext(previousContext);
            } else {
                clear();
            }
        }
    }
    
    /**
     * Create a new transaction context for the current service and set it.
     * 
     * @param serviceId the service identifier
     * @return the created transaction context
     */
    public static TransactionContext createAndSet(String serviceId) {
        TransactionContext context = TransactionContext.create(serviceId);
        setContext(context);
        return context;
    }
    
    /**
     * Update MDC with current transaction context information.
     */
    private static void updateMdc(TransactionContext context) {
        MDC.put(TRANSACTION_ID_KEY, context.transactionId());
        MDC.put(SERVICE_ID_KEY, context.serviceId());
        
        context.user().ifPresentOrElse(
            userId -> MDC.put(USER_ID_KEY, userId),
            () -> MDC.remove(USER_ID_KEY)
        );
        
        // Add additional context to MDC
        context.additionalContext().forEach((key, value) -> 
            MDC.put("context." + key, value));
    }
    
    /**
     * Clear transaction-related MDC entries.
     */
    private static void clearMdc() {
        MDC.remove(TRANSACTION_ID_KEY);
        MDC.remove(USER_ID_KEY);
        MDC.remove(SERVICE_ID_KEY);
        
        // Remove context entries (this is a simple approach, could be optimized)
        MDC.getCopyOfContextMap().keySet().stream()
            .filter(key -> key.startsWith("context."))
            .forEach(MDC::remove);
    }
}