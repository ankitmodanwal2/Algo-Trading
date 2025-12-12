package com.myorg.trading.controller;

import com.myorg.trading.broker.api.BrokerClient;
import com.myorg.trading.broker.api.BrokerPosition;
import com.myorg.trading.controller.dto.LinkBrokerRequest;
import com.myorg.trading.domain.entity.BrokerAccount;
import com.myorg.trading.service.broker.BrokerAccountService;
import com.myorg.trading.broker.registry.BrokerRegistry;
import com.myorg.trading.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/brokers")
public class BrokerController {

    private final BrokerAccountService brokerAccountService;
    private final BrokerRegistry brokerRegistry;
    private final UserService userService;

    public BrokerController(BrokerAccountService brokerAccountService,
                            BrokerRegistry brokerRegistry,
                            UserService userService) {
        this.brokerAccountService = brokerAccountService;
        this.brokerRegistry = brokerRegistry;
        this.userService = userService;
    }

    /**
     * List available broker adapters capabilities
     */
    @GetMapping("/available")
    public ResponseEntity<?> listAvailable() {
        return ResponseEntity.ok(brokerRegistry.getAll().keySet());
    }

    /**
     * Link a broker account.
     */
    @PostMapping("/link")
    public Mono<ResponseEntity<Object>> linkBroker(@AuthenticationPrincipal UserDetails user,
                                                   @RequestBody LinkBrokerRequest req) {

        BrokerClient client = brokerRegistry.getById(req.getBrokerId());

        return client.validateCredentials(req.getCredentialsJson())
                .flatMap(isValid -> {
                    BrokerAccount acc = BrokerAccount.builder()
                            .userId(getUserIdFromPrincipal(user))
                            .brokerId(req.getBrokerId())
                            .metadataJson(req.getMetadataJson())
                            .build();

                    BrokerAccount saved = brokerAccountService.saveEncryptedCredentials(acc, req.getCredentialsJson());
                    return Mono.just(ResponseEntity.ok((Object) saved));
                })
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.badRequest().body((Object) Map.of(
                            "error", "validation_failed",
                            "message", e.getMessage() != null ? e.getMessage() : "Unknown validation error"
                    )));
                });
    }

    /**
     * List all linked accounts for the current user.
     */
    @GetMapping("/linked")
    public ResponseEntity<List<BrokerAccount>> listLinked(@AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromPrincipal(user);
        List<BrokerAccount> list = brokerAccountService.listAccountsForUser(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get Open Positions from the Broker.
     * 🌟 FIX: Return List<BrokerPosition> instead of Mono<List<...>>
     * This prevents async dispatch errors (401 on /error) and ensures reliable data delivery.
     */
    @GetMapping("/{accountId}/positions")
    public List<BrokerPosition> getPositions(@AuthenticationPrincipal UserDetails user,
                                             @PathVariable Long accountId) {
        // 1. Find account and verify ownership
        BrokerAccount acc = brokerAccountService.listAccountsForUser(getUserIdFromPrincipal(user))
                .stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found or access denied"));

        // 2. Delegate to adapter and BLOCK to return data synchronously
        BrokerClient client = brokerRegistry.getById(acc.getBrokerId());

        // .block() unwraps the Mono. If an exception occurs, it is thrown here
        // and caught by GlobalExceptionHandler, preventing the 401 issue.
        return client.getPositions(accountId.toString()).block();
    }

    /**
     * Unlink/Delete a broker account.
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> unlink(@AuthenticationPrincipal UserDetails user, @PathVariable Long accountId) {
        BrokerAccount acc = brokerAccountService.listAccountsForUser(getUserIdFromPrincipal(user))
                .stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found or access denied"));

        brokerAccountService.delete(acc.getId());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{accountId}/positions/close")
    public ResponseEntity<?> closePosition(@AuthenticationPrincipal UserDetails user,
                                           @PathVariable Long accountId,
                                           @RequestBody Map<String, Object> req) {

        BrokerAccount acc = brokerAccountService.listAccountsForUser(getUserIdFromPrincipal(user))
                .stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        BrokerClient client = brokerRegistry.getById(acc.getBrokerId());

        // We use the adapter's specialized close logic
        // But since BrokerClient interface is generic, we can also use placeOrder if we construct it right.
        // However, DhanAdapter might need a specific 'closePosition' method if we want to follow reference strictly.
        // For now, let's use the 'DhanAdapter' specific method via casting or extend the Interface.

        // Better Approach: Construct a Market Order that reverses the position
        String positionType = (String) req.get("positionType"); // LONG or SHORT
        String side = "LONG".equalsIgnoreCase(positionType) ? "SELL" : "BUY";

        // Construct Order Request
        com.myorg.trading.broker.api.BrokerOrderRequest orderReq = com.myorg.trading.broker.api.BrokerOrderRequest.builder()
                .symbol((String) req.get("securityId")) // Send SecurityID as symbol!
                .quantity(new java.math.BigDecimal(req.get("quantity").toString()))
                .side(com.myorg.trading.broker.api.OrderSide.valueOf(side))
                .orderType(com.myorg.trading.broker.api.OrderType.MARKET)
                .meta(Map.of(
                        "exchange", req.get("exchange"),
                        "productType", req.get("productType")
                ))
                .build();

        return ResponseEntity.ok(client.placeOrder(accountId.toString(), orderReq).block());
    }

    private Long getUserIdFromPrincipal(UserDetails user) {
        return userService.getUserIdForUsername(user.getUsername());
    }
}