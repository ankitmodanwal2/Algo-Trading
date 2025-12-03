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
     * NOW WITH VALIDATION: Tries to login first. If login fails, returns 400 Bad Request.
     */
    @PostMapping("/link")
    public Mono<ResponseEntity<Object>> linkBroker(@AuthenticationPrincipal UserDetails user,
                                                   @RequestBody LinkBrokerRequest req) {

        // 1. Get the adapter (e.g. AngelOneAdapter)
        BrokerClient client = brokerRegistry.getById(req.getBrokerId());

        // 2. Validate Credentials FIRST (Async)
        return client.validateCredentials(req.getCredentialsJson())
                .flatMap(isValid -> {
                    // 3. Only save if validation passed
                    BrokerAccount acc = BrokerAccount.builder()
                            .userId(getUserIdFromPrincipal(user))
                            .brokerId(req.getBrokerId())
                            .metadataJson(req.getMetadataJson())
                            .build();

                    BrokerAccount saved = brokerAccountService.saveEncryptedCredentials(acc, req.getCredentialsJson());
                    // Cast body to Object to ensure type compatibility with onErrorResume (Mono<ResponseEntity<Object>>)
                    return Mono.just(ResponseEntity.ok((Object) saved));
                })
                .onErrorResume(e -> {
                    // 4. Return clear error to Frontend if validation fails
                    // Cast body to Object to match the success block
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
     */
    @GetMapping("/{accountId}/positions")
    public Mono<List<BrokerPosition>> getPositions(@AuthenticationPrincipal UserDetails user,
                                                   @PathVariable Long accountId) {
        // 1. Find account and verify ownership
        BrokerAccount acc = brokerAccountService.listAccountsForUser(getUserIdFromPrincipal(user))
                .stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found or access denied"));

        // 2. Delegate to adapter
        BrokerClient client = brokerRegistry.getById(acc.getBrokerId());
        return client.getPositions(accountId.toString());
    }

    /**
     * Unlink/Delete a broker account.
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> unlink(@AuthenticationPrincipal UserDetails user, @PathVariable Long accountId) {
        // Verify ownership before deleting
        BrokerAccount acc = brokerAccountService.listAccountsForUser(getUserIdFromPrincipal(user))
                .stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found or access denied"));

        brokerAccountService.delete(acc.getId());
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromPrincipal(UserDetails user) {
        return userService.getUserIdForUsername(user.getUsername());
    }
}