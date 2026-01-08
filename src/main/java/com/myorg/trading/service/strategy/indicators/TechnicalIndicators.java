package com.myorg.trading.service.strategy.indicators;

import com.myorg.trading.domain.model.OHLCV;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class TechnicalIndicators {

    /**
     * Calculate Simple Moving Average
     */
    public List<BigDecimal> calculateSMA(List<OHLCV> candles, int period) {
        List<BigDecimal> sma = new ArrayList<>();

        for (int i = 0; i < candles.size(); i++) {
            if (i < period - 1) {
                sma.add(null);
                continue;
            }

            BigDecimal sum = BigDecimal.ZERO;
            for (int j = 0; j < period; j++) {
                sum = sum.add(candles.get(i - j).getClose());
            }
            sma.add(sum.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP));
        }

        return sma;
    }

    /**
     * Calculate Exponential Moving Average
     */
    public List<BigDecimal> calculateEMA(List<OHLCV> candles, int period) {
        List<BigDecimal> ema = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));

        // First EMA = SMA
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < period && i < candles.size(); i++) {
            sum = sum.add(candles.get(i).getClose());
        }
        BigDecimal previousEMA = sum.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);
        ema.add(previousEMA);

        // Calculate remaining EMAs
        for (int i = 1; i < candles.size(); i++) {
            BigDecimal currentPrice = candles.get(i).getClose();
            BigDecimal currentEMA = currentPrice.subtract(previousEMA)
                    .multiply(multiplier)
                    .add(previousEMA);
            ema.add(currentEMA);
            previousEMA = currentEMA;
        }

        return ema;
    }

    /**
     * Calculate Relative Strength Index (RSI)
     */
    public List<BigDecimal> calculateRSI(List<OHLCV> candles, int period) {
        List<BigDecimal> rsi = new ArrayList<>();

        if (candles.size() < period + 1) {
            return rsi;
        }

        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();

        // Calculate price changes
        for (int i = 1; i < candles.size(); i++) {
            BigDecimal change = candles.get(i).getClose().subtract(candles.get(i - 1).getClose());
            gains.add(change.compareTo(BigDecimal.ZERO) > 0 ? change : BigDecimal.ZERO);
            losses.add(change.compareTo(BigDecimal.ZERO) < 0 ? change.abs() : BigDecimal.ZERO);
        }

        // Calculate initial average gain/loss
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;

        for (int i = 0; i < period; i++) {
            avgGain = avgGain.add(gains.get(i));
            avgLoss = avgLoss.add(losses.get(i));
        }

        avgGain = avgGain.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);

        // Calculate RSI
        for (int i = period; i < gains.size(); i++) {
            if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
                rsi.add(BigDecimal.valueOf(100));
            } else {
                BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
                BigDecimal rsiValue = BigDecimal.valueOf(100).subtract(
                        BigDecimal.valueOf(100).divide(
                                BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP
                        )
                );
                rsi.add(rsiValue);
            }

            // Update averages
            avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1))
                    .add(gains.get(i))
                    .divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);

            avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1))
                    .add(losses.get(i))
                    .divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        }

        return rsi;
    }

    /**
     * Calculate MACD (Moving Average Convergence Divergence)
     */
    public MACDResult calculateMACD(List<OHLCV> candles, int fastPeriod, int slowPeriod, int signalPeriod) {
        List<BigDecimal> fastEMA = calculateEMA(candles, fastPeriod);
        List<BigDecimal> slowEMA = calculateEMA(candles, slowPeriod);

        List<BigDecimal> macdLine = new ArrayList<>();
        for (int i = 0; i < candles.size(); i++) {
            if (i < slowPeriod) {
                macdLine.add(null);
            } else {
                macdLine.add(fastEMA.get(i).subtract(slowEMA.get(i)));
            }
        }

        // Calculate signal line (EMA of MACD line)
        List<BigDecimal> signalLine = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (signalPeriod + 1));

        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (int i = slowPeriod; i < slowPeriod + signalPeriod && i < macdLine.size(); i++) {
            if (macdLine.get(i) != null) {
                sum = sum.add(macdLine.get(i));
                count++;
            }
        }

        BigDecimal previousSignal = count > 0 ?
                sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        for (int i = 0; i < macdLine.size(); i++) {
            if (i < slowPeriod + signalPeriod || macdLine.get(i) == null) {
                signalLine.add(null);
            } else {
                BigDecimal signal = macdLine.get(i).subtract(previousSignal)
                        .multiply(multiplier)
                        .add(previousSignal);
                signalLine.add(signal);
                previousSignal = signal;
            }
        }

        // Calculate histogram
        List<BigDecimal> histogram = new ArrayList<>();
        for (int i = 0; i < macdLine.size(); i++) {
            if (macdLine.get(i) == null || signalLine.get(i) == null) {
                histogram.add(null);
            } else {
                histogram.add(macdLine.get(i).subtract(signalLine.get(i)));
            }
        }

        return new MACDResult(macdLine, signalLine, histogram);
    }

    /**
     * Calculate Bollinger Bands
     */
    public BollingerBands calculateBollingerBands(List<OHLCV> candles, int period, double stdDevMultiplier) {
        List<BigDecimal> sma = calculateSMA(candles, period);
        List<BigDecimal> upperBand = new ArrayList<>();
        List<BigDecimal> lowerBand = new ArrayList<>();

        for (int i = 0; i < candles.size(); i++) {
            if (i < period - 1) {
                upperBand.add(null);
                lowerBand.add(null);
                continue;
            }

            // Calculate standard deviation
            BigDecimal mean = sma.get(i);
            double variance = 0;
            for (int j = 0; j < period; j++) {
                double diff = candles.get(i - j).getClose().subtract(mean).doubleValue();
                variance += diff * diff;
            }
            double stdDev = Math.sqrt(variance / period);
            BigDecimal stdDevBD = BigDecimal.valueOf(stdDev);

            upperBand.add(mean.add(stdDevBD.multiply(BigDecimal.valueOf(stdDevMultiplier))));
            lowerBand.add(mean.subtract(stdDevBD.multiply(BigDecimal.valueOf(stdDevMultiplier))));
        }

        return new BollingerBands(sma, upperBand, lowerBand);
    }

    /**
     * Detect support and resistance levels
     */
    public List<BigDecimal> findSupportResistance(List<OHLCV> candles, int lookbackPeriod) {
        List<BigDecimal> levels = new ArrayList<>();

        for (int i = lookbackPeriod; i < candles.size() - lookbackPeriod; i++) {
            OHLCV current = candles.get(i);
            boolean isLocalHigh = true;
            boolean isLocalLow = true;

            // Check if it's a local high or low
            for (int j = 1; j <= lookbackPeriod; j++) {
                if (candles.get(i - j).getHigh().compareTo(current.getHigh()) > 0 ||
                        candles.get(i + j).getHigh().compareTo(current.getHigh()) > 0) {
                    isLocalHigh = false;
                }
                if (candles.get(i - j).getLow().compareTo(current.getLow()) < 0 ||
                        candles.get(i + j).getLow().compareTo(current.getLow()) < 0) {
                    isLocalLow = false;
                }
            }

            if (isLocalHigh) levels.add(current.getHigh());
            if (isLocalLow) levels.add(current.getLow());
        }

        return levels;
    }

    // Result classes
    public static class MACDResult {
        public final List<BigDecimal> macdLine;
        public final List<BigDecimal> signalLine;
        public final List<BigDecimal> histogram;

        public MACDResult(List<BigDecimal> macdLine, List<BigDecimal> signalLine, List<BigDecimal> histogram) {
            this.macdLine = macdLine;
            this.signalLine = signalLine;
            this.histogram = histogram;
        }
    }

    public static class BollingerBands {
        public final List<BigDecimal> middle;
        public final List<BigDecimal> upper;
        public final List<BigDecimal> lower;

        public BollingerBands(List<BigDecimal> middle, List<BigDecimal> upper, List<BigDecimal> lower) {
            this.middle = middle;
            this.upper = upper;
            this.lower = lower;
        }
    }
}