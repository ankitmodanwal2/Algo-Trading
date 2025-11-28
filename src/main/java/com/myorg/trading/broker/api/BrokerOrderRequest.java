package com.myorg.trading.broker.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerOrderRequest {
    /**
     * Optional client-provided idempotency / client order id.
     */
    private String clientOrderId;

    /**
     * Symbol or instrument token (canonical).
     */
    private String symbol;

    /**
     * BUY or SELL.
     */
    private OrderSide side;

    /**
     * Quantity (shares / contracts).
     */
    private BigDecimal quantity;

    /**
     * Price for LIMIT orders; null for MARKET.
     */
    private BigDecimal price;

    /**
     * MARKET or LIMIT
     */
    private OrderType orderType;

    /**
     * Time in force (GTC/IOC/FOK)
     */
    private TimeInForce timeInForce;

    /**
     * Broker-specific optional fields (e.g. product code, exchange code).
     */
    private Map<String, Object> meta;
}
