package com.myorg.trading.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "broker.fyers")
public class FyersProperties {
    private String baseUrl;
    private String apiKey;
    private String secretKey;
    private String redirectUrl;

    private String authPath = "/api/v2/generate-token";
    private String placeOrderPath = "/api/v2/orders";
    private String orderStatusPath = "/api/v2/orders/status";
    private String cancelOrderPath = "/api/v2/orders/cancel";
}
