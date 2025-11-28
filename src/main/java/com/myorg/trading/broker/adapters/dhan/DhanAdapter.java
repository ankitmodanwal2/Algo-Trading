package com.myorg.trading.broker.adapters.dhan;

import com.myorg.trading.broker.api.*;
import com.myorg.trading.config.properties.DhanProperties;
import com.myorg.trading.broker.token.TokenStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Dhan adapter skeleton. Map payloads to actual Dhan API fields as per their docs.
 */
@Component
@Qualifier("dhan")
public class DhanAdapter implements BrokerClient {

    private final WebClient webClient;
    private final DhanProperties props;
    private final TokenStore<DhanAuthResponse> tokenStore;

    public DhanAdapter(WebClient.Builder webClientBuilder, DhanProperties props,
                       TokenStore<DhanAuthResponse> tokenStore) {
        this.webClient = webClientBuilder.baseUrl(props.getBaseUrl()).build();
        this.props = props;
        this.tokenStore = tokenStore;
    }

    @Override
    public String getBrokerId() { return "dhan"; }

    @Override
    public Set<BrokerCapability> capabilities() {
        return Set.of(BrokerCapability.PLACE_ORDER, BrokerCapability.CANCEL_ORDER, BrokerCapability.MARKET_DATA_STREAM);
    }

    private Mono<DhanAuthResponse> acquireTokenIfNeeded(String accountId) {
        return tokenStore.getToken(accountId)
                .flatMap(existing -> {
                    if (existing == null || existing.isExpired()) {
                        return requestToken(accountId);
                    }
                    return Mono.just(existing);
                })
                .switchIfEmpty(requestToken(accountId));
    }

    private Mono<DhanAuthResponse> requestToken(String accountId) {
        // Placeholder: Dhan token request payload depends on Dhan API (apiKey / secret)
        Map<String, Object> body = Map.of("apiKey", props.getApiKey());
        return webClient.post()
                .uri(props.getAuthPath())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(DhanAuthResponse.class)
                .flatMap(resp -> tokenStore.saveToken(accountId, resp).thenReturn(resp));
    }

    @Override
    public Mono<BrokerAuthToken> authenticateIfNeeded() {
        // BrokerClient interface expects Mono<BrokerAuthToken> without account context.
        // For multi-account platform, adapt to accept accountId; this simple impl requests a token using a default key.
        return requestToken("default")
                .map(r -> new BrokerAuthToken(r.getAccessToken(), r.getRefreshToken(), r.getTokenType(),
                        Instant.now().plusSeconds(r.getExpiresIn())));
    }

    @Override
    public Mono<BrokerOrderResponse> placeOrder(BrokerOrderRequest req) {
        // NOTE: in multi-user system we should retrieve the broker account id and decrypt client credentials.
        // Here we assume apiKey in props or token exists.
        return authenticateIfNeeded()
                .flatMap(auth -> webClient.post()
                        .uri(props.getPlaceOrderPath())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                        .bodyValue(mapToDhanPayload(req))
                        .retrieve()
                        .bodyToMono(DhanOrderResponse.class)
                        .map(this::toBrokerOrderResponse)
                );
    }

    @Override
    public Mono<BrokerOrderStatus> getOrderStatus(String brokerOrderId) {
        return authenticateIfNeeded()
                .flatMap(auth -> webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(props.getOrderStatusPath()).queryParam("order_id", brokerOrderId).build())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                        .retrieve()
                        .bodyToMono(DhanOrderStatusResponse.class)
                        .map(this::toBrokerOrderStatus)
                );
    }

    @Override
    public Mono<Void> cancelOrder(String brokerOrderId) {
        return authenticateIfNeeded()
                .flatMap(auth -> webClient.post()
                        .uri(props.getCancelOrderPath())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                        .bodyValue(Map.of("order_id", brokerOrderId))
                        .retrieve()
                        .bodyToMono(Void.class)
                );
    }

    @Override
    public Flux<MarketDataTick> marketDataStream(String instrumentToken) {
        // Implement a WebSocket client to Dhan's streaming endpoint if available.
        return Flux.empty();
    }

    // ---- mapping helpers ----
    private Object mapToDhanPayload(BrokerOrderRequest req) {
        // TODO: adjust to actual Dhan payload
        return Map.of(
                "symbol", req.getSymbol(),
                "qty", req.getQuantity(),
                "side", req.getSide().name(),
                "type", req.getOrderType().name(),
                "price", req.getPrice()
        );
    }

    private BrokerOrderResponse toBrokerOrderResponse(DhanOrderResponse r) {
        if (r == null) return new BrokerOrderResponse(null, "REJECTED", "empty-response", Map.of());
        return new BrokerOrderResponse(r.getOrderId(), r.getStatus(), r.getMessage(), r.getRaw());
    }

    private BrokerOrderStatus toBrokerOrderStatus(DhanOrderStatusResponse s) {
        BrokerOrderStatus st = new BrokerOrderStatus();
        st.setOrderId(s.getOrderId());
        st.setStatus(s.getStatus());
        st.setFilledQuantity(s.getFilledQty());
        st.setRemainingQuantity(s.getRemainingQty());
        st.setAvgFillPrice(s.getAvgPrice());
        // map created/updated if available
        return st;
    }

    // ---- DTOs (inner or separate classes) ----
    // Create these classes in the same package or reuse a shared dto package:
    // DhanAuthResponse, DhanOrderResponse, DhanOrderStatusResponse with fields matching Dhan API.
}
