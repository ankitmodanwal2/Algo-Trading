package com.myorg.trading.service.strategy.data;

import com.myorg.trading.broker.api.BrokerClient;
import com.myorg.trading.broker.registry.BrokerRegistry;
import com.myorg.trading.domain.entity.BrokerAccount;
import com.myorg.trading.domain.model.OHLCV;
import com.myorg.trading.domain.repository.BrokerAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class MarketDataFetcher {

    private final BrokerRegistry brokerRegistry;
    private final BrokerAccountRepository brokerAccountRepository;

    public MarketDataFetcher(BrokerRegistry brokerRegistry,
                             BrokerAccountRepository brokerAccountRepository) {
        this.brokerRegistry = brokerRegistry;
        this.brokerAccountRepository = brokerAccountRepository;
    }

    public List<OHLCV> fetchHistoricalData(Long userId, String symbol, String interval, int candleCount) {
        // Get user's first broker account
        BrokerAccount account = brokerAccountRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No broker account found"));

        BrokerClient client = brokerRegistry.getById(account.getBrokerId());

        // Calculate time range based on interval and candle count
        Instant to = Instant.now();
        Instant from = to.minus(getTimeRangeForCandles(interval, candleCount), ChronoUnit.SECONDS);

        try {
            return client.getHistoricalData(
                    account.getId().toString(),
                    symbol,
                    interval,
                    from,
                    to
            ).block();
        } catch (Exception e) {
            log.error("Failed to fetch historical data for {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to fetch market data", e);
        }
    }

    private long getTimeRangeForCandles(String interval, int candleCount) {
        long intervalSeconds = switch (interval.toUpperCase()) {
            case "1M", "ONE_MINUTE" -> 60;
            case "5M", "FIVE_MINUTE" -> 300;
            case "15M", "FIFTEEN_MINUTE" -> 900;
            case "1H", "ONE_HOUR" -> 3600;
            case "1D", "ONE_DAY" -> 86400;
            default -> 300;
        };

        // Add 20% buffer for market holidays/weekends
        return (long) (intervalSeconds * candleCount * 1.2);
    }
}