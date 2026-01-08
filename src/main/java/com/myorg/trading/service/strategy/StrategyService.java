package com.myorg.trading.service.strategy;

import com.myorg.trading.domain.entity.Strategy;
import com.myorg.trading.domain.repository.StrategyRepository;
import com.myorg.trading.service.strategy.engine.StrategyExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class StrategyService {

    private final StrategyRepository strategyRepository;
    private final StrategyExecutor strategyExecutor;

    public StrategyService(StrategyRepository strategyRepository,
                           StrategyExecutor strategyExecutor) {
        this.strategyRepository = strategyRepository;
        this.strategyExecutor = strategyExecutor;
    }

    public List<Strategy> getStrategiesForUser(Long userId) {
        return strategyRepository.findByUserId(userId);
    }

    public Strategy getStrategy(Long id) {
        return strategyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found"));
    }

    @Transactional
    public Strategy createStrategy(Strategy strategy) {
        return strategyRepository.save(strategy);
    }

    @Transactional
    public Strategy updateStrategy(Long id, Strategy updates) {
        Strategy strategy = getStrategy(id);

        if (updates.getName() != null) strategy.setName(updates.getName());
        if (updates.getDescription() != null) strategy.setDescription(updates.getDescription());
        if (updates.getParamsJson() != null) strategy.setParamsJson(updates.getParamsJson());
        if (updates.getActive() != null) {
            strategy.setActive(updates.getActive());

            // Start/Stop execution
            if (updates.getActive()) {
                strategyExecutor.startStrategy(strategy);
            } else {
                strategyExecutor.stopStrategy(strategy.getId());
            }
        }

        return strategyRepository.save(strategy);
    }

    @Transactional
    public void deleteStrategy(Long id) {
        Strategy strategy = getStrategy(id);
        if (strategy.getActive()) {
            strategyExecutor.stopStrategy(id);
        }
        strategyRepository.deleteById(id);
    }

    @Transactional
    public void updateStrategyStats(Long id, Boolean wasWinningTrade, BigDecimal pnl) {
        Strategy strategy = getStrategy(id);

        strategy.setTotalTrades(strategy.getTotalTrades() + 1);
        if (wasWinningTrade) {
            strategy.setWinningTrades(strategy.getWinningTrades() + 1);
        }
        strategy.setTotalPnl(strategy.getTotalPnl().add(pnl));

        strategyRepository.save(strategy);
    }
}