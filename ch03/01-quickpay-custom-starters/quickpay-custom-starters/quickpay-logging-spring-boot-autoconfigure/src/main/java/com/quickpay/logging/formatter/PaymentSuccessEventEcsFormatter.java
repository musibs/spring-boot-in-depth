package com.quickpay.logging.formatter;

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
public class PaymentSuccessEventEcsFormatter implements StructuredLogFormatter<PaymentSuccessEvent> {

    @Value("${spring.application.name:QuickPayApplication}")
    private String applicationName;

    private final JsonWriter<PaymentSuccessEvent> writer = JsonWriter.<PaymentSuccessEvent>of((members) -> {
        members.add("transactionId", PaymentSuccessEvent::transactionId);
        members.add("customerId", PaymentSuccessEvent::customerId);
        members.add("amount", PaymentSuccessEvent::amount);
        members.add("paymentDescription", PaymentSuccessEvent::paymentDescription);
        members.add("paymentMethod", PaymentSuccessEvent::paymentMethod);
        members.add("authorizationCode", PaymentSuccessEvent::authorizationCode);
        members.add("transactionTimestamp", PaymentSuccessEvent::transactionTimestamp);
        members.add("processingTime", PaymentSuccessEvent::processingTime);

        members.add("eventType", PaymentSuccessEvent::eventType);
        members.add("eventCategory", PaymentSuccessEvent::eventCategory);
        members.add("eventAction", PaymentSuccessEvent::eventAction);
        members.add("eventOutcome", PaymentSuccessEvent::eventOutcome);

        members.add("application").usingMembers((application) -> {
            application.add("name", "applicationName");
        });
        members.add("node").usingMembers((node) -> {
            node.add("hostname", getHostname());
            node.add("ip", getLocalIpAddress());
        });
    }).withNewLineAtEnd();

    @Override
    public String format(PaymentSuccessEvent paymentSuccessEvent) {
        return this.writer.writeToString(paymentSuccessEvent);
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
