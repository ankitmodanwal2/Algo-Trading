package com.myorg.trading.broker.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface BrokerClient {

    String getBrokerId();

    Set<BrokerCapability> capabilities();

    // --- Multi-account aware methods ---

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

    /**
     * NEW: Fetch open positions (Netwise) for the specific account.
     */
    default Mono<List<BrokerPosition>> getPositions(String accountId) {
        return Mono.error(new UnsupportedOperationException(
                "getPositions() not implemented for broker: " + getBrokerId()));
    }

    default Flux<MarketDataTick> marketDataStream(String accountId, String instrumentToken) {
        return Flux.error(new UnsupportedOperationException(
                "marketDataStream() not implemented for broker: " + getBrokerId()));
    }


    // --- Backward compatibility (Single User Mode) ---
    // These methods fallback to a "default" account if the specific accountId isn't provided.

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
    default Mono<Boolean> validateCredentials(String rawCredentialsJson) {
        return Mono.just(true); // Default to true if not implemented
    }
}
