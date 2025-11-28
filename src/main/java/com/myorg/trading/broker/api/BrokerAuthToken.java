package com.myorg.trading.broker.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerAuthToken {
    private String accessToken;
    private String refreshToken;
    private String tokenType; // e.g. Bearer
    private Instant expiresAt;

    public boolean isExpired() {
        if (expiresAt == null) return true;
        // refresh a bit earlier to avoid race
        return Instant.now().isAfter(expiresAt.minusSeconds(30));
    }
}
