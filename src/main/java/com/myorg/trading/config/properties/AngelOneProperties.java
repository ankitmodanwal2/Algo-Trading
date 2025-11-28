package com.myorg.trading.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "broker.angelone")
public class AngelOneProperties {
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String apiKey;

    private String authPath = "/oauth/token";
    private String placeOrderPath = "/orders/place";
    private String orderStatusPath = "/orders/status";
    private String cancelOrderPath = "/orders/cancel";
}
