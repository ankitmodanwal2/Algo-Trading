package com.myorg.trading.broker.registry;

import com.myorg.trading.broker.api.BrokerClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of BrokerClient implementations. Spring will inject all beans implementing
 * BrokerClient and this registry maps them by getBrokerId().
 *
 * Use BrokerRegistry to obtain the adapter for a specific brokerId at runtime.
 */
@Service
public class BrokerRegistry {

    private final Map<String, BrokerClient> clients;

    public BrokerRegistry(List<BrokerClient> clientList) {
        // Build immutable map: brokerId -> client
        this.clients = clientList.stream()
                .collect(Collectors.toUnmodifiableMap(BrokerClient::getBrokerId, Function.identity()));
    }

    /**
     * Get the BrokerClient for the given brokerId, or throw IllegalArgumentException if unknown.
     */
    public BrokerClient getById(String brokerId) {
        BrokerClient c = clients.get(brokerId);
        if (c == null) {
            throw new IllegalArgumentException("Unknown broker: " + brokerId);
        }
        return c;
    }

    /**
     * Find the BrokerClient for the given brokerId, wrapped in Optional.
     */
    public Optional<BrokerClient> findById(String brokerId) {
        return Optional.ofNullable(clients.get(brokerId));
    }

    /**
     * Return an unmodifiable map of all registered clients.
     */
    public Map<String, BrokerClient> getAll() {
        return clients;
    }

    /**
     * Convenience: check if a brokerId is registered.
     */
    public boolean isRegistered(String brokerId) {
        return clients.containsKey(brokerId);
    }
}
