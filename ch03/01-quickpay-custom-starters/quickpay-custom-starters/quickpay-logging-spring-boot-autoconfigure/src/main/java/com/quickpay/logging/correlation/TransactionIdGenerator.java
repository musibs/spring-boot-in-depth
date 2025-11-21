package com.quickpay.logging.correlation;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Thread-safe utility class for generating unique transaction IDs.
 * 
 * Generates cryptographically strong, URL-safe transaction IDs with timestamp prefix
 * for natural ordering and debugging convenience.
 */
public final class TransactionIdGenerator {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    
    // Private constructor to prevent instantiation
    private TransactionIdGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Generate a unique transaction ID with format: {timestamp}-{random}
     * 
     * The timestamp prefix ensures natural ordering and helps with debugging,
     * while the random suffix ensures uniqueness across distributed systems.
     * 
     * @return a unique transaction ID string
     */
    public static String generate() {
        long timestamp = Instant.now().toEpochMilli();
        byte[] randomBytes = new byte[12]; // 96 bits of randomness
        RANDOM.nextBytes(randomBytes);
        
        String encodedRandom = ENCODER.encodeToString(randomBytes);
        
        return "txn_" + timestamp + "_" + encodedRandom;
    }
    
    /**
     * Generate a transaction ID with a custom prefix.
     * 
     * @param prefix the prefix to use instead of default "txn"
     * @return a unique transaction ID with custom prefix
     */
    public static String generateWithPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }
        
        long timestamp = Instant.now().toEpochMilli();
        byte[] randomBytes = new byte[12];
        RANDOM.nextBytes(randomBytes);
        
        String encodedRandom = ENCODER.encodeToString(randomBytes);
        
        return prefix + "_" + timestamp + "_" + encodedRandom;
    }
    
    /**
     * Validate if a string follows the expected transaction ID format.
     * 
     * @param transactionId the transaction ID to validate
     * @return true if the ID follows the expected format
     */
    public static boolean isValidFormat(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return false;
        }
        
        // Check for basic format: prefix_timestamp_random
        String[] parts = transactionId.split("_");
        if (parts.length != 3) {
            return false;
        }
        
        // Validate timestamp is numeric
        try {
            Long.parseLong(parts[1]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}