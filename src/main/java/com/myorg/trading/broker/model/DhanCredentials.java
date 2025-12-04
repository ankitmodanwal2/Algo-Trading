package com.myorg.trading.broker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DhanCredentials {
    private String clientId;    // Your Dhan Client ID (e.g., 10000001)
    private String accessToken; // The long JWT token from Dhan Web
}