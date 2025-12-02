package com.myorg.trading.broker.adapters.angelone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.trading.broker.api.MarketDataTick;
import com.myorg.trading.service.marketdata.MarketDataService;
import com.myorg.trading.web.MarketDataWebSocketController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Connects to Angel One's SmartAPI WebSocket to receive live ticks.
 */
@Slf4j
@Component
public class AngelOneWebSocketClient {

    private final MarketDataService marketDataService;
    private final MarketDataWebSocketController webSocketController;
    private final ObjectMapper objectMapper;

    // Map of AccountId -> Active Session
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public AngelOneWebSocketClient(MarketDataService marketDataService,
                                   MarketDataWebSocketController webSocketController,
                                   ObjectMapper objectMapper) {
        this.marketDataService = marketDataService;
        this.webSocketController = webSocketController;
        this.objectMapper = objectMapper;
    }

    /**
     * Connects to the WebSocket for a specific user.
     * @param authToken The JWT/Session token from Angel login
     * @param apiKey The user's API Key
     * @param clientCode The user's Client Code
     */
    public void connect(String authToken, String apiKey, String clientCode) {
        try {
            WebSocketClient client = new StandardWebSocketClient();
            // Angel One SmartAPI WebSocket URL (Confirm URL in docs as it changes)
            String wsUrl = "wss://smartapi.angelbroking.com/websocket?jwt=" + authToken + "&clientCode=" + clientCode + "&apiKey=" + apiKey;

            client.doHandshake(new AngelSocketHandler(), wsUrl);
            log.info("Initiated WebSocket connection for client: {}", clientCode);

        } catch (Exception e) {
            log.error("Failed to connect to Angel WebSocket", e);
        }
    }

    /**
     * Inner Handler to process incoming messages
     */
    private class AngelSocketHandler extends TextWebSocketHandler {
        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) {
            try {
                // Angel sends binary mostly, but if Text, parse JSON
                String payload = message.getPayload();
                JsonNode node = objectMapper.readTree(payload);

                // Parse Logic (Varies by Angel API version)
                // Assuming standard JSON tick structure:
                if (node.has("token") && node.has("ltp")) {
                    String token = node.get("token").asText();
                    BigDecimal price = new BigDecimal(node.get("ltp").asText());

                    MarketDataTick tick = new MarketDataTick(
                            token,
                            price,
                            BigDecimal.ZERO, // bid
                            BigDecimal.ZERO, // ask
                            0L,              // vol
                            Instant.now()
                    );

                    // 1. Push to Internal Service (for Algo Strategies)
                    marketDataService.pushTick(token, tick);

                    // 2. Broadcast to Frontend (for UI)
                    webSocketController.broadcastTick(token, tick);
                }

                // Keep alive / Heartbeat handling
                if (payload.contains("pong")) {
                    log.debug("Received Heartbeat");
                }

            } catch (Exception e) {
                log.error("Error parsing tick", e);
            }
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            log.info("Angel Broker WebSocket Connected!");
        }
    }
}