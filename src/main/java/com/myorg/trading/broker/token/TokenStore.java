package com.myorg.trading.broker.token;

import reactor.core.publisher.Mono;

public interface TokenStore<T> {
    Mono<T> getToken(String accountId);
    Mono<Void> saveToken(String accountId, T token);
    Mono<Void> clearToken(String accountId);
}
