package com.myorg.trading.broker.token;

import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Development-only token store. Replace with encrypted JDBC or KMS backed store for production.
 */
public class InMemoryTokenStore<T> implements TokenStore<T> {
    private final Map<String, T> store = new ConcurrentHashMap<>();

    @Override
    public Mono<T> getToken(String accountId) {
        return Mono.justOrEmpty(store.get(accountId));
    }

    @Override
    public Mono<Void> saveToken(String accountId, T token) {
        store.put(accountId, token);
        return Mono.empty();
    }

    @Override
    public Mono<Void> clearToken(String accountId) {
        store.remove(accountId);
        return Mono.empty();
    }
}
