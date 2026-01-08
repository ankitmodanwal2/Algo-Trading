package com.myorg.trading.service.strategy.backtest;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
class BacktestResult {
    private int totalTrades;
    private int winningTrades;
    private int losingTrades;
    private double winRate;
    private BigDecimal initialCapital;
    private BigDecimal finalCapital;
    private BigDecimal totalReturn;
    private BigDecimal returnPercent;
    private BigDecimal profitFactor;
    private List<Trade> trades;
}
