package com.myorg.trading.controller;

import com.myorg.trading.domain.entity.Strategy;
import com.myorg.trading.service.strategy.StrategyService;
import com.myorg.trading.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public ResponseEntity<Strategy> updateStrategy(@PathVariable Long id,
                                                   @RequestBody Strategy updates) {
        return ResponseEntity.ok(strategyService.updateStrategy(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStrategy(@PathVariable Long id) {
        strategyService.deleteStrategy(id);
        return ResponseEntity.noContent().build();
    }
}