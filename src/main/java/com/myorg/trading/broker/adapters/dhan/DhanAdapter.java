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
                        String accessToken = creds.getAccessToken().trim();

                        // Validate by fetching positions (read-only safe check)
                        return webClient.get()
                                .uri("/v2/positions") // Use v2 as per reference project
                                .header("access-token", accessToken)
                                .header("Content-Type", "application/json")
                                .retrieve()
                                .toBodilessEntity()
                                .map(resp -> resp.getStatusCode().is2xxSuccessful())
                                .onErrorResume(e -> {
                                    if (e instanceof WebClientResponseException wcre) {
                                        if (wcre.getStatusCode().value() == 401 || wcre.getStatusCode().value() == 403) {
                                            return Mono.error(new RuntimeException("Invalid Access Token"));
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
                        .uri("/v2/orders") // Use v2 explicitly
                        .header("access-token", creds.getAccessToken().trim())
                        .header("Content-Type", "application/json")
                        // Pass ClientID from credentials to payload mapper
                        .bodyValue(mapToDhanPayload(req, creds.getClientId()))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .map(this::toBrokerOrderResponse)
                );
    }

    @Override
    public Mono<List<BrokerPosition>> getPositions(String accountId) {
        return getCredentials(accountId)
                .flatMap(creds -> webClient.get()
                        .uri("/v2/positions") // Use v2 explicitly
                        .header("access-token", creds.getAccessToken().trim())
                        .header("Content-Type", "application/json")
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .map(rootNode -> {
                            List<BrokerPosition> positions = new ArrayList<>();
                            // Reference project logic: response is a List directly or inside data
                            // WebClient might map it to ArrayNode if it's a list
                            if (rootNode.isArray()) {
                                for (JsonNode node : rootNode) {
                                    // Filter out closed positions (netQty == 0) like reference project
                                    int netQty = node.path("netQty").asInt(0);
                                    if (netQty == 0) continue;

                                    positions.add(BrokerPosition.builder()
                                            .symbol(node.path("tradingSymbol").asText("Unknown"))
                                            .productType(node.path("productType").asText("INTRADAY"))
                                            .netQuantity(new BigDecimal(netQty))
                                            .avgPrice(new BigDecimal(node.path("avgPrice").asDouble(0.0)))
                                            .ltp(new BigDecimal(node.path("ltp").asDouble(0.0)))
                                            .pnl(new BigDecimal(node.path("realizedProfit").asDouble(0.0) + node.path("unrealizedProfit").asDouble(0.0)))
                                            .buyQty(new BigDecimal(node.path("buyQty").asText("0")))
                                            .sellQty(new BigDecimal(node.path("sellQty").asText("0")))
                                            .build());
                                }
                            }
                            return positions;
                        })
                );
    }

    private Map<String, Object> mapToDhanPayload(BrokerOrderRequest req, String clientId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("dhanClientId", clientId);
        payload.put("transactionType", req.getSide().name());
        payload.put("exchangeSegment", "NSE_EQ");

        // --- USE DYNAMIC PRODUCT TYPE ---
        // Default to INTRADAY if missing
        String pType = req.getMeta() != null ? (String) req.getMeta().getOrDefault("productType", "INTRADAY") : "INTRADAY";
        payload.put("productType", pType);
        // --------------------------------

        payload.put("orderType", req.getOrderType().name());
        payload.put("validity", "DAY");
        payload.put("securityId", req.getSymbol());
        payload.put("quantity", req.getQuantity());

        if (req.getPrice() != null && req.getPrice().doubleValue() > 0) {
            payload.put("price", req.getPrice());
        }
        return payload;
    }

    private BrokerOrderResponse toBrokerOrderResponse(JsonNode root) {
        String orderId = root.path("orderId").asText();
        String status = root.path("orderStatus").asText();
        return new BrokerOrderResponse(orderId, status, "Placed via Dhan", null);
    }
}