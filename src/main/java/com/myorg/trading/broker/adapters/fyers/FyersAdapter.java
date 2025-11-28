package com.myorg.trading.broker.adapters.fyers;

import com.myorg.trading.broker.api.*;
import com.myorg.trading.config.properties.FyersProperties;
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
 * Fyers adapter skeleton. Fyers supports OAuth-like token generation via appId and secret.
 */
@Component
@Qualifier("fyers")
public class FyersAdapter implements BrokerClient {

    private final WebClient webClient;
    private final FyersProperties props;
    private final TokenStore<FyersAuthResponse> tokenStore;

    public FyersAdapter(WebClient.Builder webClientBuilder, FyersProperties props,
                        TokenStore<FyersAuthResponse> tokenStore) {
        this.webClient = webClientBuilder.baseUrl(props.getBaseUrl()).build();
        this.props = props;
        this.tokenStore = tokenStore;
    }

    @Override
    public String getBrokerId() { return "fyers"; }

    @Override
    public Set<BrokerCapability> capabilities() {
        return Set.of(BrokerCapability.PLACE_ORDER, BrokerCapability.MARKET_DATA_STREAM, BrokerCapability.OCO);
    }

    private Mono<FyersAuthResponse> requestToken(String accountId) {
        // Fyers token endpoint specifics vary; adjust payload as per their docs
        Map<String, Object> body = Map.of("appId", props.getApiKey());
        return webClient.post()
                .uri(props.getAuthPath())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(FyersAuthResponse.class)
                .flatMap(r -> tokenStore.saveToken(accountId, r).thenReturn(r));
    }

    @Override
    public Mono<BrokerAuthToken> authenticateIfNeeded() {
        return requestToken("default")
                .map(r -> new BrokerAuthToken(r.getAccessToken(), r.getRefreshToken(), r.getTokenType(),
                        Instant.now().plusSeconds(r.getExpiresIn())));
    }

    @Override
    public Mono<BrokerOrderResponse> placeOrder(BrokerOrderRequest req) {
        return authenticateIfNeeded()
                .flatMap(auth -> webClient.post()
                        .uri(props.getPlaceOrderPath())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                        .bodyValue(mapToFyersPayload(req))
                        .retrieve()
                        .bodyToMono(FyersOrderResponse.class)
                        .map(this::toBrokerOrderResponse)
                );
    }

    @Override
    public Mono<BrokerOrderStatus> getOrderStatus(String brokerOrderId) {
        return authenticateIfNeeded()
                .flatMap(auth -> webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(props.getOrderStatusPath()).queryParam("id", brokerOrderId).build())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                        .retrieve()
                        .bodyToMono(FyersOrderStatusResponse.class)
                        .map(this::toBrokerOrderStatus)
                );
    }

    @Override
    public Mono<Void> cancelOrder(String brokerOrderId) {
        return authenticateIfNeeded()
                .flatMap(auth -> webClient.post()
                        .uri(props.getCancelOrderPath())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                        .bodyValue(Map.of("id", brokerOrderId))
                        .retrieve()
                        .bodyToMono(Void.class)
                );
    }

    @Override
    public Flux<MarketDataTick> marketDataStream(String instrumentToken) {
        // Implement Fyers websocket streaming if required
        return Flux.empty();
    }

    // Mapping helpers...
    private Object mapToFyersPayload(BrokerOrderRequest req) {
        // Map canonical request to Fyers API fields (example)
        return Map.of(
                "symbol", req.getSymbol(),
                "qty", req.getQuantity(),
                "type", req.getOrderType().name(),
                "side", req.getSide().name(),
                "price", req.getPrice()
        );
    }

    private BrokerOrderResponse toBrokerOrderResponse(FyersOrderResponse r) {
        if (r == null) return new BrokerOrderResponse(null, "REJECTED", "empty-response", Map.of());
        return new BrokerOrderResponse(r.getOrderId(), r.getStatus(), r.getMessage(), r.getRaw());
    }

    private BrokerOrderStatus toBrokerOrderStatus(FyersOrderStatusResponse s) {
        BrokerOrderStatus st = new BrokerOrderStatus();
        st.setOrderId(s.getOrderId());
        st.setStatus(s.getStatus());
        st.setFilledQuantity(s.getFilledQty());
        st.setAvgFillPrice(s.getAvgPrice());
        return st;
    }

    // Create FyersAuthResponse, FyersOrderResponse, FyersOrderStatusResponse DTOs as per Fyers API.
}
