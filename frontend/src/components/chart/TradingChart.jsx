import React, { useEffect, useRef, useState } from 'react';
import { createChart } from 'lightweight-charts';
import api from '../../lib/api';
import { RefreshCw, TrendingUp, Plus, Minus, Maximize2, Settings, Activity, BarChart2, TrendingDown } from 'lucide-react';
import { useRealtimeChart } from '../../hooks/useRealtimeChart';

const TradingChart = ({ symbol, tradingSymbol, onSymbolChange }) => {
    const chartContainerRef = useRef(null);
    const chartRef = useRef(null);
    const candleSeriesRef = useRef(null);
    const volumeSeriesRef = useRef(null);
    const smaSeriesRef = useRef(null);
    const emaSeriesRef = useRef(null);

    const [timeframe, setTimeframe] = useState('5M');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [lastPrice, setLastPrice] = useState(null);
    const [priceChange, setPriceChange] = useState(0);
    const [priceChangePercent, setPriceChangePercent] = useState(0);

    // Chart settings
    const [showVolume, setShowVolume] = useState(true);
    const [showSMA, setShowSMA] = useState(false);
    const [showEMA, setShowEMA] = useState(false);
    const [smaLength, setSmaLength] = useState(20);
    const [emaLength, setEmaLength] = useState(9);

    // Chart data
    const [chartData, setChartData] = useState([]);
    const [stats, setStats] = useState({ high: 0, low: 0, open: 0, volume: 0 });

    // Initialize Chart
    useEffect(() => {
        if (!chartContainerRef.current) return;

        const chart = createChart(chartContainerRef.current, {
            width: chartContainerRef.current.clientWidth,
            height: chartContainerRef.current.clientHeight,
            layout: {
                background: { color: '#0f172a' },
                textColor: '#94a3b8',
            },
            grid: {
                vertLines: { color: '#1e293b' },
                horzLines: { color: '#1e293b' },
            },
            crosshair: {
                mode: 1,
                vertLine: {
                    color: '#3b82f6',
                    width: 1,
                    style: 2,
                    labelBackgroundColor: '#3b82f6',
                },
                horzLine: {
                    color: '#3b82f6',
                    width: 1,
                    style: 2,
                    labelBackgroundColor: '#3b82f6',
                },
            },
            rightPriceScale: {
                borderColor: '#334155',
                scaleMargins: {
                    top: 0.1,
                    bottom: 0.25, // Make room for volume
                },
            },
            timeScale: {
                borderColor: '#334155',
                timeVisible: true,
                secondsVisible: false,
                rightOffset: 12,
                barSpacing: 8,
                fixLeftEdge: false,
                fixRightEdge: false,
            },
            handleScroll: {
                mouseWheel: true,
                pressedMouseMove: true,
                horzTouchDrag: true,
                vertTouchDrag: true,
            },
            handleScale: {
                axisPressedMouseMove: true,
                mouseWheel: true,
                pinch: true,
            },
        });

        // Candlestick Series
        const candleSeries = chart.addCandlestickSeries({
            upColor: '#10b981',
            downColor: '#ef4444',
            borderUpColor: '#10b981',
            borderDownColor: '#ef4444',
            wickUpColor: '#10b981',
            wickDownColor: '#ef4444',
            priceScaleId: 'right',
        });

        // Volume Series
        const volumeSeries = chart.addHistogramSeries({
            color: '#3b82f6',
            priceFormat: {
                type: 'volume',
            },
            priceScaleId: '',
            scaleMargins: {
                top: 0.85, // Volume takes bottom 15%
                bottom: 0,
            },
        });

        // SMA Series
        const smaSeries = chart.addLineSeries({
            color: '#f59e0b',
            lineWidth: 2,
            priceScaleId: 'right',
            visible: false,
        });

        // EMA Series
        const emaSeries = chart.addLineSeries({
            color: '#8b5cf6',
            lineWidth: 2,
            priceScaleId: 'right',
            visible: false,
        });

        chartRef.current = chart;
        candleSeriesRef.current = candleSeries;
        volumeSeriesRef.current = volumeSeries;
        smaSeriesRef.current = smaSeries;
        emaSeriesRef.current = emaSeries;

        // Handle Resize
        const handleResize = () => {
            if (chartContainerRef.current) {
                chart.applyOptions({
                    width: chartContainerRef.current.clientWidth,
                    height: chartContainerRef.current.clientHeight,
                });
            }
        };

        window.addEventListener('resize', handleResize);

        return () => {
            window.removeEventListener('resize', handleResize);
            chart.remove();
        };
    }, []);

    // Toggle Indicators
    useEffect(() => {
        if (smaSeriesRef.current) {
            smaSeriesRef.current.applyOptions({ visible: showSMA });
        }
        if (emaSeriesRef.current) {
            emaSeriesRef.current.applyOptions({ visible: showEMA });
        }
    }, [showSMA, showEMA]);

    // Real-time updates
    useRealtimeChart(symbol, candleSeriesRef.current, timeframe);

    // Calculate SMA
    const calculateSMA = (data, length) => {
        const sma = [];
        for (let i = length - 1; i < data.length; i++) {
            let sum = 0;
            for (let j = 0; j < length; j++) {
                sum += data[i - j].close;
            }
            sma.push({
                time: data[i].time,
                value: sum / length,
            });
        }
        return sma;
    };

    // Calculate EMA
    const calculateEMA = (data, length) => {
        const k = 2 / (length + 1);
        const ema = [];
        let emaValue = data[0].close;

        for (let i = 0; i < data.length; i++) {
            emaValue = data[i].close * k + emaValue * (1 - k);
            ema.push({
                time: data[i].time,
                value: emaValue,
            });
        }
        return ema;
    };

    // Fetch Historical Data
    useEffect(() => {
        if (!symbol || !candleSeriesRef.current) return;

        const fetchData = async () => {
            setLoading(true);
            setError(null);

            try {
                const to = Math.floor(Date.now() / 1000);
                const from = to - getTimeRangeInSeconds(timeframe);

                const response = await api.get(`/marketdata/history/${symbol}`, {
                    params: {
                        interval: timeframe,
                        from: from,
                        to: to,
                    },
                });

                const candles = response.data;

                if (candles && candles.length > 0) {
                    const chartData = candles.map(candle => ({
                        time: Math.floor(candle.time / 1000),
                        open: parseFloat(candle.open),
                        high: parseFloat(candle.high),
                        low: parseFloat(candle.low),
                        close: parseFloat(candle.close),
                    }));

                    const volumeData = candles.map(candle => ({
                        time: Math.floor(candle.time / 1000),
                        value: candle.volume,
                        color: candle.close >= candle.open ? '#10b98140' : '#ef444440',
                    }));

                    candleSeriesRef.current.setData(chartData);
                    if (showVolume) volumeSeriesRef.current.setData(volumeData);

                    setChartData(chartData);

                    // Calculate indicators
                    if (showSMA && chartData.length >= smaLength) {
                        const smaData = calculateSMA(chartData, smaLength);
                        smaSeriesRef.current.setData(smaData);
                    }

                    if (showEMA && chartData.length >= emaLength) {
                        const emaData = calculateEMA(chartData, emaLength);
                        emaSeriesRef.current.setData(emaData);
                    }

                    // Update stats
                    const lastCandle = chartData[chartData.length - 1];
                    const firstCandle = chartData[0];
                    setLastPrice(lastCandle.close);
                    const change = lastCandle.close - firstCandle.open;
                    const changePercent = (change / firstCandle.open) * 100;
                    setPriceChange(change);
                    setPriceChangePercent(changePercent);

                    const high = Math.max(...chartData.map(c => c.high));
                    const low = Math.min(...chartData.map(c => c.low));
                    const totalVolume = volumeData.reduce((sum, v) => sum + v.value, 0);

                    setStats({
                        high,
                        low,
                        open: firstCandle.open,
                        volume: totalVolume,
                    });

                    chartRef.current.timeScale().fitContent();
                } else {
                    setError('No data available for this symbol');
                }
            } catch (err) {
                console.error('Chart data fetch error:', err);
                setError(err.response?.data?.message || 'Failed to load chart data');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [symbol, timeframe, showSMA, showEMA, smaLength, emaLength]);

    const getTimeRangeInSeconds = (tf) => {
        const ranges = {
            '1M': 86400,      // 1 day
            '5M': 259200,     // 3 days
            '15M': 604800,    // 1 week
            '1H': 2592000,    // 30 days
            '1D': 31536000,   // 1 year
        };
        return ranges[tf] || 259200;
    };

    const timeframes = [
        { value: '1M', label: '1m' },
        { value: '5M', label: '5m' },
        { value: '15M', label: '15m' },
        { value: '1H', label: '1h' },
        { value: '1D', label: '1D' },
    ];

    const isPositive = priceChange >= 0;

    return (
        <div className="bg-trade-panel border border-trade-border rounded-xl h-full flex flex-col">
            {/* Header with Stats */}
            <div className="p-4 border-b border-trade-border">
                <div className="flex justify-between items-start mb-3">
                    <div>
                        <h3 className="text-lg font-bold text-white flex items-center gap-2">
                            {tradingSymbol || 'Select Symbol'}
                            {loading && <RefreshCw className="animate-spin text-trade-primary" size={16} />}
                        </h3>
                        {lastPrice && (
                            <div className="flex items-center gap-3 mt-2">
                                <span className="text-3xl font-bold text-white">
                                    ₹{lastPrice.toFixed(2)}
                                </span>
                                <div className={`flex items-center gap-1 px-2 py-1 rounded ${
                                    isPositive ? 'bg-emerald-500/20 text-emerald-400' : 'bg-red-500/20 text-red-400'
                                }`}>
                                    {isPositive ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
                                    <span className="text-sm font-bold">
                                        {isPositive ? '+' : ''}{priceChange.toFixed(2)} ({priceChangePercent.toFixed(2)}%)
                                    </span>
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Timeframe Selector */}
                    <div className="flex gap-2">
                        {timeframes.map(tf => (
                            <button
                                key={tf.value}
                                onClick={() => setTimeframe(tf.value)}
                                className={`px-3 py-1.5 rounded text-sm font-medium transition-colors ${
                                    timeframe === tf.value
                                        ? 'bg-trade-primary text-white'
                                        : 'bg-trade-bg text-trade-muted hover:text-white'
                                }`}
                            >
                                {tf.label}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Stats Bar */}
                {lastPrice && (
                    <div className="grid grid-cols-4 gap-4 text-sm">
                        <div>
                            <span className="text-trade-muted">Open</span>
                            <p className="text-white font-medium">₹{stats.open.toFixed(2)}</p>
                        </div>
                        <div>
                            <span className="text-trade-muted">High</span>
                            <p className="text-emerald-400 font-medium">₹{stats.high.toFixed(2)}</p>
                        </div>
                        <div>
                            <span className="text-trade-muted">Low</span>
                            <p className="text-red-400 font-medium">₹{stats.low.toFixed(2)}</p>
                        </div>
                        <div>
                            <span className="text-trade-muted">Volume</span>
                            <p className="text-white font-medium">{(stats.volume / 1000000).toFixed(2)}M</p>
                        </div>
                    </div>
                )}
            </div>

            {/* Indicators Toolbar */}
            <div className="px-4 py-2 border-b border-trade-border flex items-center gap-2">
                <button
                    onClick={() => setShowVolume(!showVolume)}
                    className={`px-3 py-1 rounded text-xs font-medium transition-colors ${
                        showVolume ? 'bg-blue-500/20 text-blue-400' : 'bg-trade-bg text-trade-muted hover:text-white'
                    }`}
                >
                    <BarChart2 size={14} className="inline mr-1" />
                    Volume
                </button>
                <button
                    onClick={() => setShowSMA(!showSMA)}
                    className={`px-3 py-1 rounded text-xs font-medium transition-colors ${
                        showSMA ? 'bg-amber-500/20 text-amber-400' : 'bg-trade-bg text-trade-muted hover:text-white'
                    }`}
                >
                    SMA({smaLength})
                </button>
                <button
                    onClick={() => setShowEMA(!showEMA)}
                    className={`px-3 py-1 rounded text-xs font-medium transition-colors ${
                        showEMA ? 'bg-purple-500/20 text-purple-400' : 'bg-trade-bg text-trade-muted hover:text-white'
                    }`}
                >
                    EMA({emaLength})
                </button>
                <div className="ml-auto flex gap-2">
                    <button className="p-1.5 rounded bg-trade-bg text-trade-muted hover:text-white transition-colors">
                        <Maximize2 size={16} />
                    </button>
                    <button className="p-1.5 rounded bg-trade-bg text-trade-muted hover:text-white transition-colors">
                        <Settings size={16} />
                    </button>
                </div>
            </div>

            {/* Chart Canvas */}
            <div className="flex-1 relative">
                {error && (
                    <div className="absolute inset-0 flex items-center justify-center z-10">
                        <div className="text-red-400 text-center">
                            <p className="font-medium">{error}</p>
                            <p className="text-sm text-trade-muted mt-2">
                                Try selecting a different symbol or timeframe
                            </p>
                        </div>
                    </div>
                )}

                {!symbol && !error && (
                    <div className="absolute inset-0 flex items-center justify-center">
                        <div className="text-center text-trade-muted">
                            <TrendingUp size={48} className="mx-auto mb-4 opacity-50" />
                            <p className="text-lg font-medium">Select a symbol to view chart</p>
                            <p className="text-sm mt-2">Use the order form to search for stocks</p>
                        </div>
                    </div>
                )}

                <div
                    ref={chartContainerRef}
                    className="w-full h-full"
                    style={{ minHeight: '500px' }}
                />
            </div>
        </div>
    );
};

export default TradingChart;