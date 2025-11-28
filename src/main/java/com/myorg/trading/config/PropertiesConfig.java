package com.myorg.trading.config;

import com.myorg.trading.config.properties.AngelOneProperties;
import com.myorg.trading.config.properties.DhanProperties;
import com.myorg.trading.config.properties.FyersProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        DhanProperties.class,
        FyersProperties.class,
        AngelOneProperties.class
})
public class PropertiesConfig {
}
