package com.myorg.trading.service.strategy.backtest;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BacktestRequest {
    private String symbol;
    private String interval;
    private int candleCount;
    private BigDecimal initialCapital;
    private int fastPeriod;
    private int slowPeriod;
}
