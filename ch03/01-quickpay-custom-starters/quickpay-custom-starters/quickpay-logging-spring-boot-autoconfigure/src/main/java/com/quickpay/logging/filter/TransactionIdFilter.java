package com.quickpay.logging.filter;

import com.quickpay.logging.correlation.TransactionContext;
import com.quickpay.logging.correlation.TransactionContextHolder;
import com.quickpay.logging.correlation.TransactionIdGenerator;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet filter that manages transaction correlation IDs for HTTP requests.
 * 
 * This filter extracts correlation IDs from request headers or generates new ones,
 * sets up the transaction context for the request lifecycle, and ensures proper
 * cleanup after request processing.
 * 
 * Implements Ordered to ensure it runs early in the filter chain.
 */
public class TransactionIdFilter implements Filter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionIdFilter.class);
    
    // Common header names for correlation ID
    public static final String DEFAULT_TRANSACTION_ID_HEADER = "X-Transaction-ID";
    public static final String ALT_CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String TRACE_ID_HEADER = "X-Trace-ID";
    
    private final String serviceId;
    private final String transactionIdHeader;
    private final boolean generateIfMissing;
    private final boolean addToResponse;
    
    /**
     * Create a filter with configuration options.
     */
    public TransactionIdFilter(String serviceId, String transactionIdHeader, boolean generateIfMissing, boolean addToResponse) {
        this.serviceId = serviceId != null ? serviceId : "unknown-service";
        this.transactionIdHeader = transactionIdHeader != null ? transactionIdHeader : DEFAULT_TRANSACTION_ID_HEADER;
        this.generateIfMissing = generateIfMissing;
        this.addToResponse = addToResponse;
    }
    
    /**
     * Create a filter with default configuration.
     *
     * @param serviceId the service identifier for transaction correlation
     */
    public TransactionIdFilter(String serviceId) {
        this(serviceId, DEFAULT_TRANSACTION_ID_HEADER, true, true);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if (!(request instanceof HttpServletRequest httpRequest) || 
            !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }
        
        try {
            // Extract or generate transaction ID
            String transactionId = extractTransactionId(httpRequest)
                .orElseGet(() -> generateIfMissing ? TransactionIdGenerator.generate() : null);
            
            if (transactionId != null) {
                // Create and set transaction context
                TransactionContext context = TransactionContext.of(transactionId, serviceId);
                TransactionContextHolder.setContext(context);
                
                // Add transaction ID to response headers if configured
                if (addToResponse) {
                    httpResponse.setHeader(transactionIdHeader, transactionId);
                }
                
                logger.debug("Transaction context established: transactionId={}, serviceId={}", 
                    transactionId, serviceId);
            }
            
            // Process the request
            chain.doFilter(request, response);
            
        } finally {
            // Always clean up the transaction context
            TransactionContextHolder.clear();
            logger.debug("Transaction context cleared for request");
        }
    }
    
    /**
     * Extract transaction ID from HTTP request headers.
     * 
     * Tries multiple common header names in order of preference.
     */
    private Optional<String> extractTransactionId(HttpServletRequest request) {
        // Try the configured header first
        String transactionId = request.getHeader(transactionIdHeader);
        if (isValidTransactionId(transactionId)) {
            return Optional.of(transactionId);
        }
        
        // Try alternative headers
        transactionId = request.getHeader(ALT_CORRELATION_ID_HEADER);
        if (isValidTransactionId(transactionId)) {
            return Optional.of(transactionId);
        }
        
        transactionId = request.getHeader(TRACE_ID_HEADER);
        if (isValidTransactionId(transactionId)) {
            return Optional.of(transactionId);
        }
        
        return Optional.empty();
    }
    
    /**
     * Validate that a transaction ID is not null or empty.
     */
    private boolean isValidTransactionId(String transactionId) {
        return transactionId != null && !transactionId.trim().isEmpty();
    }
    
    @Override
    public int getOrder() {
        // Run early in the filter chain, but after security filters
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing TransactionIdFilter with serviceId='{}', header='{}', generateIfMissing={}", 
            serviceId, transactionIdHeader, generateIfMissing);
    }
    
    @Override
    public void destroy() {
        logger.debug("Destroying TransactionIdFilter");
    }
}