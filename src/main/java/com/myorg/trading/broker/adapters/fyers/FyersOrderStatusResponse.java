package com.myorg.trading.broker.adapters.fyers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FyersOrderStatusResponse {
    @JsonProperty("id")
    private String orderId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("filled_quantity")
    private BigDecimal filledQty;

    @JsonProperty("remaining_quantity")
    private BigDecimal remainingQty;

    @JsonProperty("average_price")
    private BigDecimal avgPrice;

    @JsonProperty("created_time")
    private String createdTime;

    @JsonProperty("updated_time")
    private String updatedTime;
}
