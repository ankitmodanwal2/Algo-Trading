package com.myorg.trading.service.strategy.backtest;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
class Trade {
    private Instant entryTime;
    private Instant exitTime;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal quantity;
    private String side;
    private BigDecimal pnl;

    public void calculatePnL() {
        if ("BUY".equals(side)) {
            pnl = exitPrice.subtract(entryPrice).multiply(quantity);
        } else {
            pnl = entryPrice.subtract(exitPrice).multiply(quantity);
        }
    }
}