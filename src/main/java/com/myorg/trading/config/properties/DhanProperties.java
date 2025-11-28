package com.myorg.trading.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "broker.dhan")
public class DhanProperties {
    private String baseUrl;
    private String apiKey;

    // API paths (override if needed)
    private String authPath = "/auth/token";
    private String placeOrderPath = "/v1/orders";
    private String orderStatusPath = "/v1/orders/status";
    private String cancelOrderPath = "/v1/orders/cancel";
}
