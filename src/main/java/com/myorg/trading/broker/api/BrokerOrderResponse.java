package com.myorg.trading.broker.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerOrderResponse {
    /**
     * Broker-assigned order id (or null if request rejected).
     */
    private String orderId;

    /**
     * Canonical status (e.g. PENDING, PLACED, REJECTED).
     */
    private String status;

    /**
     * Human-readable message or broker message.
     */
    private String message;

    /**
     * Raw or additional broker-specific response fields.
     */
    private Map<String, Object> meta;
}
