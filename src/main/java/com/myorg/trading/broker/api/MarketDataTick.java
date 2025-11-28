package com.myorg.trading.broker.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketDataTick {
    private String instrumentToken; // canonical instrument id / symbol
    private BigDecimal lastPrice;
    private BigDecimal bid;
    private BigDecimal ask;
    private long volume;
    private Instant timestamp;
}
