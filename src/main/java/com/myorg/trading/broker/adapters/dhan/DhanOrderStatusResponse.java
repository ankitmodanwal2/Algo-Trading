package com.myorg.trading.broker.adapters.dhan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DhanOrderStatusResponse {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("status")
    private String status; // NEW, PARTIALLY_FILLED, FILLED, CANCELLED

    @JsonProperty("filled_qty")
    private BigDecimal filledQty;

    @JsonProperty("remaining_qty")
    private BigDecimal remainingQty;

    @JsonProperty("avg_price")
    private BigDecimal avgPrice;

    // Broker-specific fields
    @JsonProperty("placed_at")
    private String placedAt; // iso string — parse later if needed

    @JsonProperty("updated_at")
    private String updatedAt;
}
