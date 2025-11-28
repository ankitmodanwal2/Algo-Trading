package com.myorg.trading.broker.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerOrderStatus {
    private String orderId;
    private String status;                // e.g. NEW, PARTIALLY_FILLED, FILLED, CANCELLED
    private BigDecimal filledQuantity;
    private BigDecimal remainingQuantity;
    private BigDecimal avgFillPrice;
    private Instant createdAt;
    private Instant updatedAt;
}
