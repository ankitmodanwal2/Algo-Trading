package com.myorg.trading.broker.adapters.dhan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DhanAuthResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * seconds until expiry
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    // Raw response map if you want to persist raw JSON
    @JsonProperty("raw")
    private Object raw;

    public boolean isExpired() {
        return expiresIn == null || expiresIn <= 0;
    }
}
