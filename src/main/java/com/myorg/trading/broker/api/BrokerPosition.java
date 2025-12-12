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

    // --- NEW FIELDS REQUIRED FOR DHAN ADAPTER ---
    private String securityId;
    private String exchange;
    private String productType;
    // --------------------------------------------

    private BigDecimal netQuantity;
    private BigDecimal avgPrice;
    private BigDecimal ltp;
    private BigDecimal pnl;
    private BigDecimal buyQty;
    private BigDecimal sellQty;
}