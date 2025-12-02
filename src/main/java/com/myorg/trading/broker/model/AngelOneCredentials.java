package com.myorg.trading.broker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maps the JSON stored in BrokerAccount.credentials_encrypted for Angel One.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AngelOneCredentials {
    private String apiKey;
    private String clientCode;
    private String password;
    private String totpKey; // The Base32 secret provided by Angel One
}