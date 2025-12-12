package com.myorg.trading.broker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DhanCredentials {
    // This MUST match the JSON key from LinkBrokerModal ("clientId")
    @JsonProperty("clientId")
    private String clientId;

    // This MUST match the JSON key from LinkBrokerModal ("accessToken")
    @JsonProperty("accessToken")
    private String accessToken;
}