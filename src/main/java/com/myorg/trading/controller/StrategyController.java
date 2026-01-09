package com.myorg.trading.controller;

import com.myorg.trading.domain.entity.Strategy;
import com.myorg.trading.service.strategy.StrategyService;
import com.myorg.trading.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/strategies")
public class StrategyController {

    private final StrategyService strategyService;
    private final UserService userService;

    public StrategyController(StrategyService strategyService, UserService userService) {
        this.strategyService = strategyService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Strategy>> listStrategies(@AuthenticationPrincipal UserDetails user) {
        Long userId = userService.getUserIdForUsername(user.getUsername());
        return ResponseEntity.ok(strategyService.getStrategiesForUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Strategy> getStrategy(@PathVariable Long id) {
        return ResponseEntity.ok(strategyService.getStrategy(id));
    }

    @PostMapping
    public ResponseEntity<Strategy> createStrategy(@AuthenticationPrincipal UserDetails user,
                                                   @RequestBody Strategy strategy) {
        Long userId = userService.getUserIdForUsername(user.getUsername());
        strategy.setUserId(userId);
        return ResponseEntity.ok(strategyService.createStrategy(strategy));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStrategy(@PathVariable Long id,
                                            @RequestBody Strategy updates) {
        try {
            Strategy updated = strategyService.updateStrategy(id, updates);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Failed to update strategy {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "update_failed", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStrategy(@PathVariable Long id) {
        strategyService.deleteStrategy(id);
        return ResponseEntity.noContent().build();
    }
}