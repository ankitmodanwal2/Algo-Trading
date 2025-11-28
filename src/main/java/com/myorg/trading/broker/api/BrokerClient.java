package com.myorg.trading.broker.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface BrokerClient {

    String getBrokerId();

    Set<BrokerCapability> capabilities();

    // Multi-account aware methods
    default Mono<BrokerAuthToken> authenticateIfNeeded(String accountId) {
        return Mono.error(new UnsupportedOperationException(
                "authenticateIfNeeded() not implemented for broker: " + getBrokerId()));
    }

    default Mono<BrokerOrderResponse> placeOrder(String accountId, BrokerOrderRequest req) {
        return Mono.error(new UnsupportedOperationException(
                "placeOrder() not implemented for broker: " + getBrokerId()));
    }

    default Mono<BrokerOrderStatus> getOrderStatus(String accountId, String brokerOrderId) {
        return Mono.error(new UnsupportedOperationException(
                "getOrderStatus() not implemented for broker: " + getBrokerId()));
    }

    default Mono<Void> cancelOrder(String accountId, String brokerOrderId) {
        return Mono.error(new UnsupportedOperationException(
                "cancelOrder() not implemented for broker: " + getBrokerId()));
    }

    default Flux<MarketDataTick> marketDataStream(String accountId, String instrumentToken) {
        return Flux.error(new UnsupportedOperationException(
                "marketDataStream() not implemented for broker: " + getBrokerId()));
    }


    // Backward compatibility — old methods fallback to "default" account
    default Mono<BrokerAuthToken> authenticateIfNeeded() {
        return authenticateIfNeeded("default");
    }

    default Mono<BrokerOrderResponse> placeOrder(BrokerOrderRequest req) {
        return placeOrder("default", req);
    }

    default Mono<BrokerOrderStatus> getOrderStatus(String brokerOrderId) {
        return getOrderStatus("default", brokerOrderId);
    }

    default Mono<Void> cancelOrder(String brokerOrderId) {
        return cancelOrder("default", brokerOrderId);
    }

    default Flux<MarketDataTick> marketDataStream(String instrumentToken) {
        return marketDataStream("default", instrumentToken);
    }
}
