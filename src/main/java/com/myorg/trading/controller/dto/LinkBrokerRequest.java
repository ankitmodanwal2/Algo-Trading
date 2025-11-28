package com.myorg.trading.controller.dto;

import lombok.Data;

/**
 * credentialsJson: a JSON string containing broker-specific credentials (apiKey, secret).
 * metadataJson: optional metadata fields (display name, account number).
 */
@Data
public class LinkBrokerRequest {
    private String brokerId;
    private String credentialsJson;
    private String metadataJson;
}
