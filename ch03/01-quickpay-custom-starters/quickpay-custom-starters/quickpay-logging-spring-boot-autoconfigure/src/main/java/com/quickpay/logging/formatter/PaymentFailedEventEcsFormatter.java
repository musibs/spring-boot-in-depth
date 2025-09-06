package com.quickpay.logging.formatter;

import com.quickpay.logging.domain.PaymentFailedEvent;
import com.quickpay.logging.domain.PaymentSuccessEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLogFormatter;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Somnath Musib
 * Date: 10/08/2025
 */
@Service
public class PaymentFailedEventEcsFormatter implements StructuredLogFormatter<PaymentFailedEvent> {

    @Value("${spring.application.name:QuickPayApplication}")
    private String applicationName;

    private final JsonWriter<PaymentFailedEvent> writer = JsonWriter.<PaymentFailedEvent>of((members) -> {
        members.add("transactionId", PaymentFailedEvent::transactionId);
        members.add("customerId", PaymentFailedEvent::customerId);
        members.add("amount", PaymentFailedEvent::amount);
        members.add("paymentDescription", PaymentFailedEvent::paymentDescription);
        members.add("paymentMethod", PaymentFailedEvent::paymentMethod);
        members.add("errorCode", PaymentFailedEvent::errorCode);
        members.add("errorReason", PaymentFailedEvent::errorMessage);
        members.add("retryAttempt", PaymentFailedEvent::retryAttempt);
        members.add("failureReason", PaymentFailedEvent::failureReason);

        members.add("eventType", PaymentFailedEvent::eventType);
        members.add("eventCategory", PaymentFailedEvent::eventCategory);
        members.add("eventAction", PaymentFailedEvent::eventAction);
        members.add("eventOutcome", PaymentFailedEvent::eventOutcome);


        members.add("application").usingMembers((application) -> {
            application.add("name", "applicationName");
        });
        members.add("node").usingMembers((node) -> {
            node.add("hostname", getHostname());
            node.add("ip", getLocalIpAddress());
        });
    }).withNewLineAtEnd();

    @Override
    public String format(PaymentFailedEvent event) {
        return this.writer.writeToString(event);
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }

    private String getLocalIpAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown-ip";
        }
    }
}
