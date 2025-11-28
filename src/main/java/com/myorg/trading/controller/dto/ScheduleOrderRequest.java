package com.myorg.trading.controller.dto;

import com.myorg.trading.broker.api.OrderSide;
import com.myorg.trading.broker.api.OrderType;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ScheduleOrderRequest {
    @NotNull
    private Long brokerAccountId;

    @NotNull
    private String symbol;

    @NotNull
    private OrderSide side;

    @NotNull
    private BigDecimal quantity;

    private BigDecimal price;

    @NotNull
    private OrderType orderType;

    /**
     * Timestamp when order should be executed (ISO instant). Client should send epoch millis or ISO string parsed into Instant by client.
     */
    @NotNull
    private Instant triggerTime;
}
