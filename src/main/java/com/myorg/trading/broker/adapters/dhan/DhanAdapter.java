package com.myorg.trading.broker.adapters.dhan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.trading.broker.api.*;
import com.myorg.trading.broker.model.DhanCredentials;
import com.myorg.trading.config.properties.DhanProperties;
import com.myorg.trading.service.broker.BrokerAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component("dhan")
public class DhanAdapter implements BrokerClient {

    private final WebClient webClient;
    private final DhanProperties props;
    private final BrokerAccountService brokerAccountService;
    private final ObjectMapper objectMapper;

    public DhanAdapter(WebClient.Builder webClientBuilder,
                       DhanProperties props,
                       BrokerAccountService brokerAccountService,
                       ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(props.getBaseUrl()).build();
        this.props = props;
        this.brokerAccountService = brokerAccountService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getBrokerId() { return "dhan"; }

    @Override
    public Set<BrokerCapability> capabilities() {
        return Set.of(BrokerCapability.PLACE_ORDER, BrokerCapability.CANCEL_ORDER);
    }

    private Mono<DhanCredentials> getCredentials(String accountId) {
        return Mono.fromCallable(() -> brokerAccountService.readDecryptedCredentials(Long.valueOf(accountId)))
                .flatMap(opt -> opt.map(Mono::just).orElse(Mono.error(new IllegalArgumentException("No credentials found for account: " + accountId))))
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, DhanCredentials.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse Dhan credentials", e);
                    }
                });
    }

    @Override
    public Mono<Boolean> validateCredentials(String rawCredentialsJson) {
        return Mono.just(rawCredentialsJson)
                .flatMap(json -> {
                    try {
                        DhanCredentials creds = objectMapper.readValue(json, DhanCredentials.class);
                        String clientId = creds.getClientId().trim();
                        String accessToken = creds.getAccessToken().trim();

                        return webClient.get()
                                .uri("/orders")
                                .header("access-token", accessToken)
                                .header("client-id", clientId)
                                .header("Content-Type", "application/json")
                                .retrieve()
                                .toBodilessEntity()
                                .map(resp -> resp.getStatusCode().is2xxSuccessful())
                                .onErrorResume(e -> {
                                    if (e instanceof WebClientResponseException wcre) {
                                        if (wcre.getStatusCode().value() == 401 || wcre.getStatusCode().value() == 403) {
                                            return Mono.error(new RuntimeException("Invalid Client ID or Access Token"));
                                        }
                                    }
                                    return Mono.error(new RuntimeException("Dhan API Error: " + e.getMessage()));
                                });
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Dhan Validation Error: " + e.getMessage()));
                    }
                });
    }

    @Override
    public Mono<BrokerOrderResponse> placeOrder(String accountId, BrokerOrderRequest req) {
        return getCredentials(accountId)
                .flatMap(creds -> webClient.post()
                        .uri(props.getPlaceOrderPath())
                        .header("access-token", creds.getAccessToken().trim())
                        .header("client-id", creds.getClientId().trim())
                        .header("Content-Type", "application/json")
                        .bodyValue(mapToDhanPayload(req))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .map(this::toBrokerOrderResponse)
                );
    }

    @Override
    public Mono<List<BrokerPosition>> getPositions(String accountId) {
        return getCredentials(accountId)
                .flatMap(creds -> webClient.get()
                        .uri("/positions")
                        .header("access-token", creds.getAccessToken().trim())
                        .header("client-id", creds.getClientId().trim())
                        .header("Content-Type", "application/json")
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .map(rootNode -> {
                            List<BrokerPosition> positions = new ArrayList<>();
                            // Handle different JSON structures safely
                            JsonNode dataNode = rootNode.has("data") ? rootNode.get("data") : rootNode;

                            if (dataNode.isArray()) {
                                for (JsonNode node : dataNode) {
                                    positions.add(BrokerPosition.builder()
                                            .symbol(node.path("tradingSymbol").asText("Unknown"))
                                            .productType(node.path("productType").asText("INTRADAY"))
                                            .netQuantity(new BigDecimal(node.path("netQty").asText("0")))
                                            .avgPrice(new BigDecimal(node.path("avgPrice").asText("0"))) // Use avgPrice or avgCostPrice depending on Dhan version
                                            .ltp(new BigDecimal(node.path("ltp").asText("0")))
                                            .pnl(new BigDecimal(node.path("realizedProfitLoss").asDouble(0.0) + node.path("unrealizedProfitLoss").asDouble(0.0)))
                                            .buyQty(new BigDecimal(node.path("buyQty").asText("0")))
                                            .sellQty(new BigDecimal(node.path("sellQty").asText("0")))
                                            .build());
                                }
                            }
                            return positions;
                        })
                );
    }

    private Map<String, Object> mapToDhanPayload(BrokerOrderRequest req) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("dhanClientId", "YOUR_CLIENT_ID_placeholder");
        payload.put("transactionType", req.getSide().name());
        payload.put("exchangeSegment", "NSE_EQ");
        payload.put("productType", "INTRADAY");
        payload.put("orderType", req.getOrderType().name());
        payload.put("validity", "DAY");
        payload.put("securityId", req.getSymbol());
        payload.put("quantity", req.getQuantity());
        payload.put("price", req.getPrice());
        return payload;
    }

    private BrokerOrderResponse toBrokerOrderResponse(JsonNode root) {
        String orderId = root.path("orderId").asText();
        String status = root.path("orderStatus").asText();
        return new BrokerOrderResponse(orderId, status, "Placed via Dhan", null);
    }
}