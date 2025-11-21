package com.quickpay.logging.formatter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLogFormatter;

import com.quickpay.logging.correlation.TransactionContextHolder;

/**
 * ECS-compliant structured log formatter for QuickPay applications.
 * 
 * This formatter implements the Elastic Common Schema (ECS) specification
 * and integrates with QuickPay's transaction correlation system to provide
 * consistent, searchable log entries across all services.
 * 
 * Key ECS fields supported:
 * - @timestamp: ISO 8601 timestamp
 * - ecs.version: ECS specification version
 * - service.name/version/environment: Service identification
 * - transaction.id: QuickPay transaction correlation
 * - user.id: User identification when available
 * - host.hostname/ip: Host information
 * - log.level/logger: Standard logging metadata
 */
public class QuickPayEcsFormatter implements StructuredLogFormatter<Map<String, Object>> {
    
    private static final String ECS_VERSION = "8.11";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "password", "token", "secret", "key", "credential", "authorization",
        "card", "account", "ssn", "email", "phone"
    );
    
    private final String serviceName;
    private final String serviceVersion;
    private final String serviceEnvironment;
    private final String hostname;
    private final String hostIp;
    private final boolean enablePiiMasking;

    private final JsonWriter<Map<String, Object>> writer;
    
    public QuickPayEcsFormatter(String serviceName, String serviceVersion, String serviceEnvironment, boolean enablePiiMasking) {
        this.serviceName = serviceName != null ? serviceName : "quickpay-service";
        this.serviceVersion = serviceVersion != null ? serviceVersion : "unknown";
        this.serviceEnvironment = serviceEnvironment != null ? serviceEnvironment : "unknown";
        this.enablePiiMasking = enablePiiMasking;
        this.hostname = getHostname();
        this.hostIp = getHostIp();
        
        this.writer = createJsonWriter();
    }
    
    @Override
    public String format(Map<String, Object> event) {
        // Add core ECS fields
        event.put("@timestamp", Instant.now().toString());
        event.put("ecs", Map.of("version", ECS_VERSION));

        // Add host information
        event.put("host", Map.of(
            "hostname", hostname,
            "ip", hostIp
        ));

        // Add QuickPay-specific transaction context
        enrichWithTransactionContext(event);

        if (enablePiiMasking) {
            maskSensitiveData(event);
        }

        return writer.writeToString(event);
    }
    
    /**
     * Create the JSON writer with ECS field mapping.
     */
    private JsonWriter<Map<String, Object>> createJsonWriter() {
        return JsonWriter.<Map<String, Object>>of((members) -> {
            // Core ECS fields
            members.add("@timestamp");
            members.add("ecs").usingMembers((ecs) -> {
                ecs.add("version");
            });

            // Host information
            members.add("host").usingMembers((host) -> {
                host.add("hostname");
                host.add("ip");
            });

            // Log metadata
            members.add("log").usingMembers((log) -> {
                log.add("level");
                log.add("logger");
            });

            // Process information
            members.add("process").usingMembers((process) -> {
                process.add("pid");
                process.add("thread").usingMembers((thread) -> {
                    thread.add("name");
                });
            });

            // QuickPay-specific fields
            members.add("transaction").whenNotNull();
            members.add("user").whenNotNull();
            members.add("labels").whenNotNull();

            // Message and other fields
            members.add("message");
            members.add("tags").whenNotNull();

        }).withNewLineAtEnd();
    }
    
    
    /**
     * Enrich event with transaction context from ThreadLocal.
     */
    private void enrichWithTransactionContext(Map<String, Object> event) {
        TransactionContextHolder.getTransactionId().ifPresent(transactionId -> {
            Map<String, Object> transaction = Map.of("id", transactionId);
            event.put("transaction", transaction);
        });
        
        TransactionContextHolder.getUserId().ifPresent(userId -> {
            Map<String, Object> user = Map.of("id", userId);
            event.put("user", user);
        });
        
        // Add MDC context as labels
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        if (mdc != null && !mdc.isEmpty()) {
            Map<String, String> labels = mdc.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("context."))
                .collect(Collectors.toMap(
                    entry -> entry.getKey().substring(8), // Remove "context." prefix
                    Map.Entry::getValue
                ));
            
            if (!labels.isEmpty()) {
                event.put("labels", labels);
            }
        }
    }
    
    
    
    /**
     * Mask sensitive data in the event.
     */
    private void maskSensitiveData(Map<String, Object> event) {
        event.entrySet().forEach(entry -> {
            String key = entry.getKey().toLowerCase();
            if (SENSITIVE_FIELDS.stream().anyMatch(key::contains)) {
                if (entry.getValue() instanceof String stringValue) {
                    event.put(entry.getKey(), maskString(stringValue));
                }
            }
        });
    }
    
    /**
     * Mask sensitive string data.
     */
    private String maskString(String value) {
        if (value == null || value.length() <= 4) {
            return "***";
        }
        
        // Show first and last 2 characters, mask the middle
        String start = value.substring(0, 2);
        String end = value.substring(value.length() - 2);
        String middle = "*".repeat(Math.min(value.length() - 4, 8));
        
        return start + middle + end;
    }
    
    
    /**
     * Get hostname with fallback.
     */
    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }
    
    /**
     * Get host IP address with fallback.
     */
    private String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown-ip";
        }
    }
}