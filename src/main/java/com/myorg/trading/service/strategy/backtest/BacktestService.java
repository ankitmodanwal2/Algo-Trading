package com.myorg.trading.service.strategy.backtest;

import com.myorg.trading.domain.model.OHLCV;
import com.myorg.trading.service.strategy.data.MarketDataFetcher;
import com.myorg.trading.service.strategy.indicators.TechnicalIndicators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BacktestService {

    private final TechnicalIndicators indicators;
    private final MarketDataFetcher dataFetcher;

    public BacktestService(TechnicalIndicators indicators, MarketDataFetcher dataFetcher) {
        this.indicators = indicators;
        this.dataFetcher = dataFetcher;
    }

    public BacktestResult runBacktest(Long userId, BacktestRequest request) {
        List<OHLCV> candles = dataFetcher.fetchHistoricalData(
                userId,
                request.getSymbol(),
                request.getInterval(),
                request.getCandleCount()
        );

        List<Trade> trades = new ArrayList<>();
        BigDecimal capital = request.getInitialCapital();
        Trade openTrade = null;

        // Calculate indicators
        List<BigDecimal> fastSMA = indicators.calculateSMA(candles, request.getFastPeriod());
        List<BigDecimal> slowSMA = indicators.calculateSMA(candles, request.getSlowPeriod());

        for (int i = request.getSlowPeriod(); i < candles.size() - 1; i++) {
            if (fastSMA.get(i) == null || slowSMA.get(i) == null) continue;

            BigDecimal currentFast = fastSMA.get(i);
            BigDecimal currentSlow = slowSMA.get(i);
            BigDecimal prevFast = fastSMA.get(i - 1);
            BigDecimal prevSlow = slowSMA.get(i - 1);

            // Detect crossover
            boolean bullishCross = prevFast.compareTo(prevSlow) < 0 && currentFast.compareTo(currentSlow) > 0;
            boolean bearishCross = prevFast.compareTo(prevSlow) > 0 && currentFast.compareTo(currentSlow) < 0;

            // Entry
            if (openTrade == null && bullishCross) {
                openTrade = new Trade();
                openTrade.setEntryTime(candles.get(i).getTimestamp());
                openTrade.setEntryPrice(candles.get(i).getClose());
                openTrade.setQuantity(capital.divide(candles.get(i).getClose(), 0, RoundingMode.DOWN));
                openTrade.setSide("BUY");
            }

            // Exit
            if (openTrade != null && bearishCross) {
                openTrade.setExitTime(candles.get(i).getTimestamp());
                openTrade.setExitPrice(candles.get(i).getClose());
                openTrade.calculatePnL();

                capital = capital.add(openTrade.getPnl());
                trades.add(openTrade);
                openTrade = null;
            }
        }

        // Calculate statistics
        return calculateStatistics(trades, request.getInitialCapital(), capital);
    }

    private BacktestResult calculateStatistics(List<Trade> trades, BigDecimal initialCapital, BigDecimal finalCapital) {
        BacktestResult result = new BacktestResult();
        result.setTotalTrades(trades.size());
        result.setInitialCapital(initialCapital);
        result.setFinalCapital(finalCapital);
        result.setTotalReturn(finalCapital.subtract(initialCapital));
        result.setReturnPercent(
                finalCapital.subtract(initialCapital)
                        .divide(initialCapital, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
        );

        long winningTrades = trades.stream().filter(t -> t.getPnl().compareTo(BigDecimal.ZERO) > 0).count();
        result.setWinningTrades((int) winningTrades);
        result.setLosingTrades(trades.size() - (int) winningTrades);
        result.setWinRate(trades.isEmpty() ? 0.0 : (winningTrades * 100.0) / trades.size());

        BigDecimal totalProfit = trades.stream()
                .filter(t -> t.getPnl().compareTo(BigDecimal.ZERO) > 0)
                .map(Trade::getPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLoss = trades.stream()
                .filter(t -> t.getPnl().compareTo(BigDecimal.ZERO) < 0)
                .map(Trade::getPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.setProfitFactor(
                totalLoss.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                        totalProfit.divide(totalLoss.abs(), 2, RoundingMode.HALF_UP)
        );

        result.setTrades(trades);

        return result;
    }
}
