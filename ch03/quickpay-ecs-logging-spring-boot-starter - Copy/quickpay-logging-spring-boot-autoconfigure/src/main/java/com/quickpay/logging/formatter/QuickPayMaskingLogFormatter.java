package com.quickpay.logging.formatter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLogFormatter;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom structured log formatter with masking support for sensitive fields.
 * Masks only sensitive argument values in log messages and MDC.
 */
public class QuickPayMaskingLogFormatter implements StructuredLogFormatter<ILoggingEvent> {

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "token", "secret", "key", "credential", "authorization",
            "card", "account", "ssn", "email", "phone"
    );

    private final boolean enableMasking;
    private final String hostname;
    private final String hostIp;
    private final JsonWriter<LogData> writer;

    public QuickPayMaskingLogFormatter(Environment environment) {
        this.enableMasking = environment.getProperty("quickpay.logging.pii-masking", Boolean.class, Boolean.TRUE);
        this.hostname = resolveHostname();
        this.hostIp = resolveHostIp();
        this.writer = createJsonWriter();
    }

    @Override
    public String format(ILoggingEvent event) {
        Object[] args = event.getArgumentArray();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null && isSensitive(args[i].toString())) {
                    args[i] = maskString(args[i].toString());
                }
            }
        }
        String message = event.getMessage();
        if (args != null && args.length > 0) {
            message = String.format(convertToPrintf(message), args);
        }

        // Mask MDC / labels
        Map<String, String> mdc = event.getMDCPropertyMap();
        Map<String, String> labels = null;
        if (mdc != null && !mdc.isEmpty()) {
            labels = mdc.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> maskStringIfSensitive(e.getValue())
                    ));
        }

        LogData logData = new LogData(
                Instant.ofEpochMilli(event.getTimeStamp()).toString(),
                new HostInfo(hostname, hostIp),
                new LogInfo(event.getLevel().toString(), event.getLoggerName()),
                new ProcessInfo(ProcessHandle.current().pid(), new ThreadInfo(Thread.currentThread().getName())),
                message,
                labels
        );

        return writer.writeToString(logData);
    }

    private JsonWriter<LogData> createJsonWriter() {
        return JsonWriter.<LogData>of((members) -> {
            members.add("@timestamp", LogData::timestamp);
            members.add("host").usingMembers(host -> {
                host.add("hostname");
                host.add("ip");
            });
            members.add("log").usingMembers(log -> {
                log.add("level");
                log.add("logger");
            });
            members.add("process").usingMembers(process -> {
                process.add("pid");
                process.add("thread").usingMembers(thread -> thread.add("name"));
            });
            members.add("message");
            members.add("labels").whenNotNull();
        }).withNewLineAtEnd();
    }

    /** Check if the value contains any sensitive keyword */
    private boolean isSensitive(String value) {
        if (!enableMasking || value == null) return false;
        String lower = value.toLowerCase();
        return SENSITIVE_FIELDS.stream().anyMatch(lower::contains);
    }

    /** Mask value leaving first and last 2 characters */
    private String maskString(String value) {
        if (value == null || value.length() <= 4) return "***";
        return value.substring(0, 2) + "*".repeat(value.length() - 4) + value.substring(value.length() - 2);
    }

    /** Mask MDC value if sensitive */
    private String maskStringIfSensitive(String value) {
        return isSensitive(value) ? maskString(value) : value;
    }

    /** Convert SLF4J-style {} placeholders into printf-style %s for String.format */
    private String convertToPrintf(String message) {
        return message.replace("{}", "%s");
    }

    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }

    private String resolveHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown-ip";
        }
    }

    public record LogData(
            String timestamp,
            HostInfo host,
            LogInfo log,
            ProcessInfo process,
            String message,
            Map<String, String> labels
    ) {}

    public record HostInfo(String hostname, String ip) {}

    public record LogInfo(String level, String logger) {}

    public record ProcessInfo(long pid, ThreadInfo thread) {}

    public record ThreadInfo(String name) {}
}
