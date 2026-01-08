package com.myorg.trading.service.strategy.impl;

import java.math.BigDecimal;

public class TradeSignal {
    public static final TradeSignal HOLD = new TradeSignal(TradeAction.HOLD, null, null, null, null);

    private final TradeAction action;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final BigDecimal stopLoss;
    private final BigDecimal target;

    public TradeSignal(TradeAction action, BigDecimal price, BigDecimal quantity,
                       BigDecimal stopLoss, BigDecimal target) {
        this.action = action;
        this.price = price;
        this.quantity = quantity;
        this.stopLoss = stopLoss;
        this.target = target;
    }

    public TradeAction getAction() { return action; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getStopLoss() { return stopLoss; }
    public BigDecimal getTarget() { return target; }

    public boolean shouldTrade() {
        return action != TradeAction.HOLD;
    }
}

enum TradeAction {
    BUY, SELL, HOLD
}