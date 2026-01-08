package com.myorg.trading.service.strategy.impl;

import com.myorg.trading.broker.api.*;
import com.myorg.trading.domain.entity.Strategy;
import com.myorg.trading.domain.model.OHLCV;
import com.myorg.trading.service.strategy.data.MarketDataFetcher;
import com.myorg.trading.service.strategy.indicators.TechnicalIndicators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SMACrossoverStrategy {

    private final TechnicalIndicators indicators;
    private final MarketDataFetcher dataFetcher;

    public SMACrossoverStrategy(TechnicalIndicators indicators, MarketDataFetcher dataFetcher) {
        this.indicators = indicators;
        this.dataFetcher = dataFetcher;
    }

    public TradeSignal evaluate(Strategy strategy, Map<String, Object> params, BrokerClient client, String accountId) {
        try {
            // Extract parameters
            int fastSMA = (int) params.getOrDefault("fastSMA", 9);
            int slowSMA = (int) params.getOrDefault("slowSMA", 21);
            String symbol = (String) params.get("symbol");
            int quantity = (int) params.getOrDefault("quantity", 1);
            double stopLoss = (double) params.getOrDefault("stopLoss", 2.0);
            double target = (double) params.getOrDefault("target", 4.0);

            // Fetch historical data
            List<OHLCV> candles = dataFetcher.fetchHistoricalData(
                    strategy.getUserId(),
                    symbol,
                    "5M",
                    Math.max(fastSMA, slowSMA) + 50
            );

            if (candles.size() < slowSMA) {
                log.warn("Insufficient data for SMA calculation");
                return TradeSignal.HOLD;
            }

            // Calculate SMAs
            List<BigDecimal> fastSMAValues = indicators.calculateSMA(candles, fastSMA);
            List<BigDecimal> slowSMAValues = indicators.calculateSMA(candles, slowSMA);

            // Get latest values
            int lastIndex = candles.size() - 1;
            int prevIndex = lastIndex - 1;

            if (fastSMAValues.get(lastIndex) == null || slowSMAValues.get(lastIndex) == null ||
                    fastSMAValues.get(prevIndex) == null || slowSMAValues.get(prevIndex) == null) {
                return TradeSignal.HOLD;
            }

            BigDecimal currentFast = fastSMAValues.get(lastIndex);
            BigDecimal currentSlow = slowSMAValues.get(lastIndex);
            BigDecimal prevFast = fastSMAValues.get(prevIndex);
            BigDecimal prevSlow = slowSMAValues.get(prevIndex);

            // Detect crossover
            boolean bullishCross = prevFast.compareTo(prevSlow) < 0 && currentFast.compareTo(currentSlow) > 0;
            boolean bearishCross = prevFast.compareTo(prevSlow) > 0 && currentFast.compareTo(currentSlow) < 0;

            if (bullishCross) {
                log.info("Bullish SMA crossover detected for {}", symbol);
                return new TradeSignal(
                        TradeAction.BUY,
                        candles.get(lastIndex).getClose(),
                        BigDecimal.valueOf(quantity),
                        calculateStopLoss(candles.get(lastIndex).getClose(), stopLoss, false),
                        calculateTarget(candles.get(lastIndex).getClose(), target, true)
                );
            } else if (bearishCross) {
                log.info("Bearish SMA crossover detected for {}", symbol);
                return new TradeSignal(
                        TradeAction.SELL,
                        candles.get(lastIndex).getClose(),
                        BigDecimal.valueOf(quantity),
                        calculateStopLoss(candles.get(lastIndex).getClose(), stopLoss, true),
                        calculateTarget(candles.get(lastIndex).getClose(), target, false)
                );
            }

            return TradeSignal.HOLD;

        } catch (Exception e) {
            log.error("Error evaluating SMA crossover strategy: {}", e.getMessage(), e);
            return TradeSignal.HOLD;
        }
    }

    private BigDecimal calculateStopLoss(BigDecimal price, double percentage, boolean isLong) {
        BigDecimal factor = BigDecimal.ONE.subtract(BigDecimal.valueOf(percentage / 100));
        if (!isLong) {
            factor = BigDecimal.ONE.add(BigDecimal.valueOf(percentage / 100));
        }
        return price.multiply(factor);
    }

    private BigDecimal calculateTarget(BigDecimal price, double percentage, boolean isLong) {
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(percentage / 100));
        if (!isLong) {
            factor = BigDecimal.ONE.subtract(BigDecimal.valueOf(percentage / 100));
        }
        return price.multiply(factor);
    }
}