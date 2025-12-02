package com.myorg.trading.broker.adapters.angelone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.trading.broker.api.*;
import com.myorg.trading.broker.model.AngelOneCredentials;
import com.myorg.trading.broker.token.TokenStore;
import com.myorg.trading.config.properties.AngelOneProperties;
import com.myorg.trading.service.broker.BrokerAccountService;
import com.myorg.trading.util.CryptoUtil; // <--- IMPT: Import this
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

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
    private final AngelOneWebSocketClient wsClient; // <--- IMPT: Inject WebSocket Client

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
                        // 1. Parse JSON
                        AngelOneCredentials creds = objectMapper.readValue(json, AngelOneCredentials.class);

                        // 2. GENERATE TOTP (Fixing the hardcoded value)
                        String totp = CryptoUtil.generateTotp(creds.getTotpKey());

                        Map<String, Object> loginBody = Map.of(
                                "clientcode", creds.getClientCode(),
                                "password", creds.getPassword(),
                                "totp", totp // <--- Sending real TOTP
                        );

                        // 3. Call Broker API
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

                                    // 4. Connect WebSocket immediately (Fixing the missing link)
                                    wsClient.connect(response.getAccessToken(), creds.getApiKey(), creds.getClientCode());

                                    return tokenStore.saveToken(accountId, response).thenReturn(response);
                                });

                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Login Flow Failed", e));
                    }
                });
    }

    // --- Order Placement ---

    @Override
    public Mono<BrokerOrderResponse> placeOrder(String accountId, BrokerOrderRequest req) {
        return authenticateAccount(accountId)
                .flatMap(auth -> {
                    return Mono.fromCallable(() -> brokerAccountService.readDecryptedCredentials(Long.valueOf(accountId)))
                            .map(opt -> opt.get())
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

    // --- Mappers ---

    private Object mapToAngelPayload(BrokerOrderRequest req) {
        // Map.of() has a limit of 10 entries. We use HashMap for >10 entries.
        java.util.Map<String, Object> payload = new java.util.HashMap<>();

        payload.put("variety", "NORMAL"); // or AMO / STOPLOSS
        payload.put("tradingsymbol", req.getSymbol());
        payload.put("symboltoken", req.getMeta() != null ? req.getMeta().get("token") : "3045");
        payload.put("transactiontype", req.getSide().name()); // BUY / SELL
        payload.put("exchange", "NSE");
        payload.put("ordertype", req.getOrderType().name()); // MARKET / LIMIT
        payload.put("producttype", "INTRADAY"); // CARRYFORWARD / MARGIN / INTRADAY
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
}