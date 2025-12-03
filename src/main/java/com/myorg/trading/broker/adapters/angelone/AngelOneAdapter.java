package com.myorg.trading.broker.adapters.angelone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.trading.broker.api.*;
import com.myorg.trading.broker.model.AngelOneCredentials;
import com.myorg.trading.broker.token.TokenStore;
import com.myorg.trading.config.properties.AngelOneProperties;
import com.myorg.trading.service.broker.BrokerAccountService;
import com.myorg.trading.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component("angelone")
public class AngelOneAdapter implements BrokerClient {

    private final WebClient webClient;
    private final AngelOneProperties props;
    private final TokenStore<AngelAuthResponse> tokenStore;
    private final BrokerAccountService brokerAccountService;
    private final ObjectMapper objectMapper;
    private final AngelOneWebSocketClient wsClient;

    public AngelOneAdapter(WebClient.Builder webClientBuilder,
                           AngelOneProperties props,
                           TokenStore<AngelAuthResponse> tokenStore,
                           BrokerAccountService brokerAccountService,
                           ObjectMapper objectMapper,
                           AngelOneWebSocketClient wsClient) {
        this.webClient = webClientBuilder.baseUrl(props.getBaseUrl()).build();
        this.props = props;
        this.tokenStore = tokenStore;
        this.brokerAccountService = brokerAccountService;
        this.objectMapper = objectMapper;
        this.wsClient = wsClient;
    }

    @Override
    public String getBrokerId() { return "angelone"; }

    @Override
    public Set<BrokerCapability> capabilities() {
        return Set.of(BrokerCapability.PLACE_ORDER, BrokerCapability.MARKET_DATA_STREAM);
    }

    // --- Authentication Logic ---

    private Mono<AngelAuthResponse> authenticateAccount(String accountId) {
        return tokenStore.getToken(accountId)
                .filter(t -> !t.isExpired())
                .switchIfEmpty(Mono.defer(() -> performLogin(accountId)));
    }

    private Mono<AngelAuthResponse> performLogin(String accountId) {
        return Mono.fromCallable(() -> brokerAccountService.readDecryptedCredentials(Long.valueOf(accountId)))
                .flatMap(opt -> opt.map(Mono::just).orElse(Mono.error(new IllegalArgumentException("No credentials found for account: " + accountId))))
                .flatMap(json -> {
                    try {
                        AngelOneCredentials creds = objectMapper.readValue(json, AngelOneCredentials.class);

                        // --- DEBUG LOGGING START ---
                        log.info("--- LOGIN ATTEMPT (Account {}) ---", accountId);
                        log.info("Client Code: {}", creds.getClientCode());
                        log.info("API Key Length: {} (Should be > 30 chars)", creds.getApiKey() != null ? creds.getApiKey().length() : 0);
                        log.info("TOTP Key Length: {} (Should be ~16-32 chars)", creds.getTotpKey() != null ? creds.getTotpKey().length() : 0);
                        // ----------------------------

                        String totp = CryptoUtil.generateTotp(creds.getTotpKey());
                        log.info("Generated TOTP: {}", totp); // Check if this matches your phone app!

                        Map<String, Object> loginBody = Map.of(
                                "clientcode", creds.getClientCode(),
                                "password", creds.getPassword(),
                                "totp", totp
                        );

                        return webClient.post()
                                .uri(props.getAuthPath())
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("X-PrivateKey", creds.getApiKey())
                                .header("X-User-Agent", "Java/1.0")
                                .header("X-Client-LocalIP", "127.0.0.1")
                                .header("X-Client-PublicIP", "127.0.0.1")
                                .header("X-MACAddress", "mac_address")
                                .bodyValue(loginBody)
                                .retrieve()
                                .bodyToMono(AngelAuthResponse.class)
                                .flatMap(response -> {
                                    if (response.getAccessToken() == null) {
                                        return Mono.error(new RuntimeException("Login failed: " + response.getMessage()));
                                    }
                                    response.markObtainedNow();
                                    log.info("LOGIN SUCCESSFUL! Connecting WebSocket...");
                                    wsClient.connect(response.getAccessToken(), creds.getApiKey(), creds.getClientCode());
                                    return tokenStore.saveToken(accountId, response).thenReturn(response);
                                });

                    } catch (Exception e) {
                        log.error("LOGIN FAILED: {}", e.getMessage());
                        return Mono.error(new RuntimeException("Login Flow Failed: " + e.getMessage(), e));
                    }
                });
    }

    // --- Order Placement ---

    @Override
    public Mono<BrokerOrderResponse> placeOrder(String accountId, BrokerOrderRequest req) {
        return authenticateAccount(accountId)
                .flatMap(auth -> {
                    return Mono.fromCallable(() -> brokerAccountService.readDecryptedCredentials(Long.valueOf(accountId)))
                            .flatMap(opt -> opt.map(Mono::just).orElse(Mono.error(new IllegalArgumentException("No credentials found for account: " + accountId))))
                            .map(json -> {
                                try { return objectMapper.readValue(json, AngelOneCredentials.class).getApiKey(); }
                                catch(Exception e) { throw new RuntimeException(e); }
                            })
                            .flatMap(apiKey ->
                                    webClient.post()
                                            .uri(props.getPlaceOrderPath())
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                                            .header("X-PrivateKey", apiKey)
                                            .bodyValue(mapToAngelPayload(req))
                                            .retrieve()
                                            .bodyToMono(AngelOrderResponse.class)
                                            .map(this::toBrokerOrderResponse)
                            );
                });
    }

    // --- Position Fetching (NEW) ---

    @Override
    public Mono<List<BrokerPosition>> getPositions(String accountId) {
        return authenticateAccount(accountId)
                .flatMap(auth -> {
                    return Mono.fromCallable(() -> brokerAccountService.readDecryptedCredentials(Long.valueOf(accountId)))
                            .flatMap(opt -> opt.map(Mono::just).orElse(Mono.error(new IllegalArgumentException("No credentials found for account: " + accountId))))
                            .map(json -> {
                                try { return objectMapper.readValue(json, AngelOneCredentials.class).getApiKey(); }
                                catch(Exception e) { throw new RuntimeException(e); }
                            })
                            .flatMap(apiKey ->
                                    webClient.get()
                                            .uri("https://apiconnect.angelbroking.com/rest/secure/angelbroking/order/v1/getPosition")
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                                            .header("X-PrivateKey", apiKey)
                                            .header("Accept", "application/json")
                                            .header("X-User-Agent", "Java/1.0")
                                            .header("X-Client-LocalIP", "127.0.0.1")
                                            .header("X-Client-PublicIP", "127.0.0.1")
                                            .header("X-MACAddress", "mac_address")
                                            .retrieve()
                                            .bodyToMono(JsonNode.class)
                                            .map(rootNode -> {
                                                JsonNode dataNode = rootNode.path("data");
                                                if (dataNode.isMissingNode() || !dataNode.isArray()) {
                                                    return List.<BrokerPosition>of();
                                                }

                                                List<BrokerPosition> positions = new ArrayList<>();
                                                for (JsonNode node : dataNode) {
                                                    positions.add(BrokerPosition.builder()
                                                            .symbol(node.path("tradingsymbol").asText())
                                                            .productType(node.path("producttype").asText())
                                                            .netQuantity(new BigDecimal(node.path("netqty").asText("0")))
                                                            .avgPrice(new BigDecimal(node.path("avgnetprice").asText("0")))
                                                            .ltp(new BigDecimal(node.path("ltp").asText("0")))
                                                            .pnl(new BigDecimal(node.path("pnl").asText("0")))
                                                            .buyQty(new BigDecimal(node.path("buyqty").asText("0")))
                                                            .sellQty(new BigDecimal(node.path("sellqty").asText("0")))
                                                            .build());
                                                }
                                                return positions;
                                            })
                            );
                });
    }

    // --- Mappers ---

    private Object mapToAngelPayload(BrokerOrderRequest req) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("variety", "NORMAL");
        payload.put("tradingsymbol", req.getSymbol());
        payload.put("symboltoken", req.getMeta() != null ? req.getMeta().get("token") : "3045");
        payload.put("transactiontype", req.getSide().name());
        payload.put("exchange", "NSE");
        payload.put("ordertype", req.getOrderType().name());
        payload.put("producttype", "INTRADAY");
        payload.put("duration", "DAY");
        payload.put("price", req.getPrice());
        payload.put("squareoff", "0");
        payload.put("stoploss", "0");
        payload.put("quantity", req.getQuantity());

        return payload;
    }

    private BrokerOrderResponse toBrokerOrderResponse(AngelOrderResponse r) {
        if (r == null) return new BrokerOrderResponse(null, "REJECTED", "empty", Map.of());
        return new BrokerOrderResponse(r.getOrderId(), r.getStatus(), r.getMessage(), r.getRaw());
    }
    @Override
    public Mono<Boolean> validateCredentials(String rawCredentialsJson) {
        return Mono.just(rawCredentialsJson)
                .flatMap(json -> {
                    try {
                        // 1. Parse the credentials passed from UI
                        AngelOneCredentials creds = objectMapper.readValue(json, AngelOneCredentials.class);
                        String totp = CryptoUtil.generateTotp(creds.getTotpKey());

                        Map<String, Object> loginBody = Map.of(
                                "clientcode", creds.getClientCode(),
                                "password", creds.getPassword(),
                                "totp", totp
                        );

                        // 2. Try Login (Stateless call)
                        return webClient.post()
                                .uri(props.getAuthPath())
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("X-PrivateKey", creds.getApiKey())
                                .header("X-User-Agent", "Java/1.0")
                                .header("X-Client-LocalIP", "127.0.0.1")
                                .header("X-Client-PublicIP", "127.0.0.1")
                                .header("X-MACAddress", "mac_address")
                                .bodyValue(loginBody)
                                .retrieve()
                                .bodyToMono(AngelAuthResponse.class)
                                .map(response -> {
                                    if (response.getAccessToken() == null) {
                                        throw new RuntimeException("Validation Failed: " + response.getMessage());
                                    }
                                    return true; // Login Success!
                                });

                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Invalid Credentials: " + e.getMessage()));
                    }
                });
    }
}