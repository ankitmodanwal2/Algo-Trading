package com.myorg.trading.broker.adapters.angelone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AngelOrderStatusResponse {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("filled_qty")
    private BigDecimal filledQty;

    @JsonProperty("remaining_qty")
    private BigDecimal remainingQty;

    @JsonProperty("avg_price")
    private BigDecimal avgPrice;

    @JsonProperty("placed_at")
    private String placedAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
