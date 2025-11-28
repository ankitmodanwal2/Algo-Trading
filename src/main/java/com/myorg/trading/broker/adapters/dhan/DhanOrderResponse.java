package com.myorg.trading.broker.adapters.dhan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DhanOrderResponse {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("status")
    private String status; // e.g., placed / rejected

    @JsonProperty("message")
    private String message;

    /**
     * any other raw metadata the broker returns
     */
    @JsonProperty("meta")
    private Map<String, Object> raw;
}
