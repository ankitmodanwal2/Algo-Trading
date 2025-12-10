package com.myorg.trading.controller.dto;

import com.myorg.trading.broker.api.OrderSide;
import com.myorg.trading.broker.api.OrderType;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PlaceOrderRequest {
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

    // --- NEW FIELD ---
    private String productType; // INTRADAY, CNC, etc.
}