package com.myorg.trading.controller;

import com.myorg.trading.controller.dto.LinkBrokerRequest;
import com.myorg.trading.domain.entity.BrokerAccount;
import com.myorg.trading.service.broker.BrokerAccountService;
import com.myorg.trading.broker.registry.BrokerRegistry;
import com.myorg.trading.service.user.UserService; // <--- NEW IMPORT
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brokers")
public class BrokerController {

    private final BrokerAccountService brokerAccountService;
    private final BrokerRegistry brokerRegistry;
    private final UserService userService; // <--- NEW FIELD

    public BrokerController(BrokerAccountService brokerAccountService,
                            BrokerRegistry brokerRegistry,
                            UserService userService) { // <--- NEW ARGUMENT
        this.brokerAccountService = brokerAccountService;
        this.brokerRegistry = brokerRegistry;
        this.userService = userService;
    }

    /**
     * List available broker adapters and their capabilities
     */
    @GetMapping("/available")
    public ResponseEntity<?> listAvailable() {
        return ResponseEntity.ok(brokerRegistry.getAll().keySet());
    }

    /**
     * Link a broker account.
     */
    @PostMapping("/link")
    public ResponseEntity<?> linkBroker(@AuthenticationPrincipal UserDetails user,
                                        @RequestBody LinkBrokerRequest req) {
        BrokerAccount acc = BrokerAccount.builder()
                .userId(getUserIdFromPrincipal(user))
                .brokerId(req.getBrokerId())
                .metadataJson(req.getMetadataJson())
                .build();

        BrokerAccount saved = brokerAccountService.saveEncryptedCredentials(acc, req.getCredentialsJson());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/linked")
    public ResponseEntity<List<BrokerAccount>> listLinked(@AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromPrincipal(user);
        List<BrokerAccount> list = brokerAccountService.listAccountsForUser(userId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> unlink(@AuthenticationPrincipal UserDetails user, @PathVariable Long accountId) {
        brokerAccountService.delete(accountId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromPrincipal(UserDetails user) {
        // FIX: Look up ID from database instead of parsing username
        return userService.getUserIdForUsername(user.getUsername());
    }
}