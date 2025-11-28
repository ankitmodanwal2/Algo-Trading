package com.myorg.trading.controller;

import com.myorg.trading.controller.dto.LinkBrokerRequest;
import com.myorg.trading.domain.entity.BrokerAccount;
import com.myorg.trading.service.broker.BrokerAccountService;
import com.myorg.trading.broker.registry.BrokerRegistry;
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

    public BrokerController(BrokerAccountService brokerAccountService, BrokerRegistry brokerRegistry) {
        this.brokerAccountService = brokerAccountService;
        this.brokerRegistry = brokerRegistry;
    }

    /**
     * List available broker adapters and their capabilities
     */
    @GetMapping("/available")
    public ResponseEntity<?> listAvailable() {
        return ResponseEntity.ok(brokerRegistry.getAll().keySet());
    }

    /**
     * Link a broker account. Frontend should POST credentials securely (HTTPS).
     * credentialsJson is a JSON string containing broker-specific fields (apiKey, secret, etc.)
     */
    @PostMapping("/link")
    public ResponseEntity<?> linkBroker(@AuthenticationPrincipal UserDetails user,
                                        @RequestBody LinkBrokerRequest req) {
        // map to BrokerAccount entity and encrypt credentials in BrokerAccountService
        BrokerAccount acc = BrokerAccount.builder()
                .userId(getUserIdFromPrincipal(user))
                .brokerId(req.getBrokerId())
                .metadataJson(req.getMetadataJson())
                .build();

        BrokerAccount saved = brokerAccountService.saveEncryptedCredentials(acc, req.getCredentialsJson());
        return ResponseEntity.ok(saved);
    }

    /**
     * List linked broker accounts for current user.
     */
    @GetMapping("/linked")
    public ResponseEntity<List<BrokerAccount>> listLinked(@AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromPrincipal(user);
        List<BrokerAccount> list = brokerAccountService.listAccountsForUser(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Unlink a broker account
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> unlink(@AuthenticationPrincipal UserDetails user, @PathVariable Long accountId) {
        // TODO: authorize that this account belongs to user
        brokerAccountService.delete(accountId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromPrincipal(UserDetails user) {
        // This helper assumes username is unique; lookup userId from username if needed.
        // For now we guess username equals id if numeric; adapt to your auth model.
        try {
            return Long.parseLong(user.getUsername());
        } catch (NumberFormatException ex) {
            // In many implementations username != userId. If so, you should inject UserRepository and map username->id.
            throw new IllegalStateException("Principal username is not numeric. Adapt BrokerController.getUserIdFromPrincipal()");
        }
    }
}
