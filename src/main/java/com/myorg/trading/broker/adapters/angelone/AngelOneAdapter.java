package com.myorg.trading.broker.adapters.angelone;

import com.myorg.trading.broker.api.*;
import com.myorg.trading.config.properties.AngelOneProperties;
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
 * Angel One SmartAPI adapter (skeleton).
 * Angel uses session tokens + refresh flows; refer to their SmartAPI docs for exact fields.
 */
@Component
@Qualifier("angelone")
public class AngelOneAdapter implements BrokerClient {

    private final WebClient webClient;
    private final AngelOneProperties props;
    private final TokenStore<AngelAuthResponse> tokenStore;

    public AngelOneAdapter(WebClient.Builder webClientBuilder, AngelOneProperties props,
                           TokenStore<AngelAuthResponse> tokenStore) {
        this.webClient = webClientBuilder.baseUrl(props.getBaseUrl()).build();
        this.props = props;
        this.tokenStore = tokenStore;
    }

    @Override
    public String getBrokerId() { return "angelone"; }

    @Override
    public Set<BrokerCapability> capabilities() {
        return Set.of(BrokerCapability.PLACE_ORDER, BrokerCapability.CANCEL_ORDER, BrokerCapability.MARKET_DATA_STREAM);
    }

    private Mono<AngelAuthResponse> authenticateAccount(String accountId) {
        return tokenStore.getToken(accountId)
                .flatMap(t -> {
                    if (t == null || t.isExpired()) {
                        return requestNewToken(accountId);
                    }
                    return Mono.just(t);
                })
                .switchIfEmpty(requestNewToken(accountId));
    }

    private Mono<AngelAuthResponse> requestNewToken(String accountId) {
        Map<String, Object> body = Map.of("client_id", props.getClientId(), "client_secret", props.getClientSecret());
        return webClient.post()
                .uri(props.getAuthPath())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AngelAuthResponse.class)
                .flatMap(r -> tokenStore.saveToken(accountId, r).thenReturn(r));
    }

    @Override
    public Mono<BrokerAuthToken> authenticateIfNeeded() {
        return requestNewToken("default")
                .map(r -> new BrokerAuthToken(r.getAccessToken(), r.getRefreshToken(), r.getTokenType(),
                        Instant.now().plusSeconds(r.getExpiresIn())));
    }

    @Override
    public Mono<BrokerOrderResponse> placeOrder(BrokerOrderRequest req) {
        return authenticateIfNeeded()
                .flatMap(auth -> webClient.post()
                        .uri(props.getPlaceOrderPath())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                        .bodyValue(mapToAngelPayload(req))
                        .retrieve()
                        .bodyToMono(AngelOrderResponse.class)
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
                        .bodyToMono(AngelOrderStatusResponse.class)
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
        // Build WebSocket client to angelone market feed or use broker-provided streaming SDK.
        return Flux.empty();
    }

    // --- mapping helpers ---
    private Object mapToAngelPayload(BrokerOrderRequest req) {
        return Map.of(
                "symbol", req.getSymbol(),
                "qty", req.getQuantity(),
                "side", req.getSide().name(),
                "orderType", req.getOrderType().name(),
                "price", req.getPrice()
        );
    }

    private BrokerOrderResponse toBrokerOrderResponse(AngelOrderResponse r) {
        if (r == null) return new BrokerOrderResponse(null, "REJECTED", "empty", Map.of());
        return new BrokerOrderResponse(r.getOrderId(), r.getStatus(), r.getMessage(), r.getRaw());
    }

    private BrokerOrderStatus toBrokerOrderStatus(AngelOrderStatusResponse s) {
        BrokerOrderStatus st = new BrokerOrderStatus();
        st.setOrderId(s.getOrderId());
        st.setStatus(s.getStatus());
        st.setFilledQuantity(s.getFilledQty());
        st.setAvgFillPrice(s.getAvgPrice());
        return st;
    }

    // Create DTOs AngelAuthResponse, AngelOrderResponse, AngelOrderStatusResponse according to SmartAPI docs.
}
