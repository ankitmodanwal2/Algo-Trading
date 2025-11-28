package com.myorg.trading.broker.token;

import com.myorg.trading.broker.adapters.dhan.DhanAuthResponse;
import com.myorg.trading.broker.adapters.fyers.FyersAuthResponse;
import com.myorg.trading.broker.adapters.angelone.AngelAuthResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenStoreConfig {

    @Bean
    public TokenStore<DhanAuthResponse> dhanTokenStore() {
        return new InMemoryTokenStore<>();
    }

    @Bean
    public TokenStore<FyersAuthResponse> fyersTokenStore() {
        return new InMemoryTokenStore<>();
    }

    @Bean
    public TokenStore<AngelAuthResponse> angelTokenStore() {
        return new InMemoryTokenStore<>();
    }
}
