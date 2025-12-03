package com.myorg.trading.broker.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerPosition {
    private String symbol;
    private String productType;   // INTRADAY / CARRYFORWARD
    private BigDecimal netQuantity;
    private BigDecimal avgPrice;
    private BigDecimal ltp;       // Last Traded Price
    private BigDecimal pnl;       // Profit and Loss
    private BigDecimal buyQty;
    private BigDecimal sellQty;
}