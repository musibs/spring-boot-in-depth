package com.quickpay.client;

import com.quickpay.domain.PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry for managing multiple payment provider clients.
 * Demonstrates ObjectProvider.orderedStream() usage for collecting beans.
 * <p>
 * Uses LinkedHashMap to preserve the insertion order from @Order annotations.
 */
public class PaymentProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(PaymentProviderRegistry.class);

    private final Map<PaymentProvider, PaymentProviderClient> clients;

    /**
     * Constructor accepting a list of provider clients.
     * Typically populated via ObjectProvider.orderedStream().toList()
     * <p>
     * The order of clients in the list is preserved (respecting @Order annotations).
     *
     * @param clients list of payment provider clients (ordered)
     */
    public PaymentProviderRegistry(List<PaymentProviderClient> clients) {
        // Use LinkedHashMap to preserve insertion order from orderedStream()
        this.clients = clients.stream()
                .collect(Collectors.toMap(
                        PaymentProviderClient::getProvider,
                        Function.identity(),
                        (existing, replacement) -> existing,  // merge function (shouldn't happen)
                        LinkedHashMap::new  // preserve order
                ));

        log.info("Initialized PaymentProviderRegistry with {} providers: {}",
                 clients.size(),
                 this.clients.keySet());
    }

    /**
     * Get a client for the specified provider
     *
     * @param provider the payment provider
     * @return optional containing the client if available
     */
    public Optional<PaymentProviderClient> getClient(PaymentProvider provider) {
        return Optional.ofNullable(clients.get(provider));
    }

    /**
     * Get all registered providers
     *
     * @return list of registered payment providers
     */
    public List<PaymentProvider> getRegisteredProviders() {
        return List.copyOf(clients.keySet());
    }

    /**
     * Get all healthy providers
     *
     * @return list of healthy payment providers
     */
    public List<PaymentProvider> getHealthyProviders() {
        return clients.values().stream()
                .filter(PaymentProviderClient::isHealthy)
                .map(PaymentProviderClient::getProvider)
                .toList();
    }

    /**
     * Check if a provider is registered
     *
     * @param provider the payment provider
     * @return true if registered, false otherwise
     */
    public boolean isProviderRegistered(PaymentProvider provider) {
        return clients.containsKey(provider);
    }
}
