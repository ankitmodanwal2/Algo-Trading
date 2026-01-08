import React, { useState } from 'react';
import { Play, TrendingUp, TrendingDown, Activity, DollarSign, Percent, Target } from 'lucide-react';
import api from '../../lib/api';
import toast from 'react-hot-toast';

const Backtester = ({ strategy }) => {
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);
    const [params, setParams] = useState({
        symbol: 'NSE:RELIANCE',
        interval: '5M',
        candleCount: 500,
        initialCapital: 100000,
        fastPeriod: 9,
        slowPeriod: 21,
    });

    const runBacktest = async () => {
        setLoading(true);
        try {
            const res = await api.post('/strategies/backtest', params);
            setResult(res.data);
            toast.success('Backtest completed!');
        } catch (err) {
            toast.error('Backtest failed: ' + (err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(value);
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h3 className="text-xl font-bold text-white">Strategy Backtester</h3>
                    <p className="text-trade-muted mt-1">Test your strategy on historical data</p>
                </div>
            </div>

            {/* Configuration Form */}
            <div className="bg-trade-panel border border-trade-border rounded-xl p-6">
                <h4 className="text-lg font-semibold text-white mb-4">Backtest Parameters</h4>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm text-trade-muted mb-1">Symbol</label>
                        <input
                            type="text"
                            value={params.symbol}
                            onChange={(e) => setParams({ ...params, symbol: e.target.value })}
                            className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white"
                            placeholder="NSE:RELIANCE"
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-trade-muted mb-1">Interval</label>
                        <select
                            value={params.interval}
                            onChange={(e) => setParams({ ...params, interval: e.target.value })}
                            className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white"
                        >
                            <option value="1M">1 Minute</option>
                            <option value="5M">5 Minutes</option>
                            <option value="15M">15 Minutes</option>
                            <option value="1H">1 Hour</option>
                            <option value="1D">1 Day</option>
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm text-trade-muted mb-1">Candle Count</label>
                        <input
                            type="number"
                            value={params.candleCount}
                            onChange={(e) => setParams({ ...params, candleCount: parseInt(e.target.value) })}
                            className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white"
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-trade-muted mb-1">Initial Capital (₹)</label>
                        <input
                            type="number"
                            value={params.initialCapital}
                            onChange={(e) => setParams({ ...params, initialCapital: parseFloat(e.target.value) })}
                            className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white"
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-trade-muted mb-1">Fast SMA Period</label>
                        <input
                            type="number"
                            value={params.fastPeriod}
                            onChange={(e) => setParams({ ...params, fastPeriod: parseInt(e.target.value) })}
                            className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white"
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-trade-muted mb-1">Slow SMA Period</label>
                        <input
                            type="number"
                            value={params.slowPeriod}
                            onChange={(e) => setParams({ ...params, slowPeriod: parseInt(e.target.value) })}
                            className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white"
                        />
                    </div>
                </div>

                <button
                    onClick={runBacktest}
                    disabled={loading}
                    className="w-full mt-6 bg-trade-primary hover:bg-blue-600 text-white px-6 py-3 rounded-lg font-medium transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
                >
                    {loading ? (
                        <>
                            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                            Running Backtest...
                        </>
                    ) : (
                        <>
                            <Play size={18} />
                            Run Backtest
                        </>
                    )}
                </button>
            </div>

            {/* Results */}
            {result && (
                <div className="space-y-4">
                    {/* Summary Cards */}
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                        <div className="bg-trade-panel border border-trade-border rounded-xl p-5">
                            <div className="flex items-center gap-3 mb-2">
                                <div className="p-2 bg-blue-500/20 rounded-lg">
                                    <Activity size={20} className="text-blue-400" />
                                </div>
                                <span className="text-trade-muted text-sm">Total Trades</span>
                            </div>
                            <p className="text-2xl font-bold text-white">{result.totalTrades}</p>
                        </div>

                        <div className="bg-trade-panel border border-trade-border rounded-xl p-5">
                            <div className="flex items-center gap-3 mb-2">
                                <div className="p-2 bg-emerald-500/20 rounded-lg">
                                    <Percent size={20} className="text-emerald-400" />
                                </div>
                                <span className="text-trade-muted text-sm">Win Rate</span>
                            </div>
                            <p className="text-2xl font-bold text-emerald-400">{result.winRate.toFixed(1)}%</p>
                            <p className="text-xs text-trade-muted mt-1">
                                {result.winningTrades}W / {result.losingTrades}L
                            </p>
                        </div>

                        <div className="bg-trade-panel border border-trade-border rounded-xl p-5">
                            <div className="flex items-center gap-3 mb-2">
                                <div className="p-2 bg-purple-500/20 rounded-lg">
                                    <DollarSign size={20} className="text-purple-400" />
                                </div>
                                <span className="text-trade-muted text-sm">Total Return</span>
                            </div>
                            <p className={`text-2xl font-bold ${result.totalReturn >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
                                {result.totalReturn >= 0 ? '+' : ''}{formatCurrency(result.totalReturn)}
                            </p>
                            <p className="text-xs text-trade-muted mt-1">
                                {result.returnPercent >= 0 ? '+' : ''}{result.returnPercent.toFixed(2)}%
                            </p>
                        </div>

                        <div className="bg-trade-panel border border-trade-border rounded-xl p-5">
                            <div className="flex items-center gap-3 mb-2">
                                <div className="p-2 bg-amber-500/20 rounded-lg">
                                    <Target size={20} className="text-amber-400" />
                                </div>
                                <span className="text-trade-muted text-sm">Profit Factor</span>
                            </div>
                            <p className="text-2xl font-bold text-white">{result.profitFactor.toFixed(2)}</p>
                            <p className="text-xs text-trade-muted mt-1">
                                {result.profitFactor > 1 ? 'Profitable' : 'Unprofitable'}
                            </p>
                        </div>
                    </div>

                    {/* Detailed Results */}
                    <div className="bg-trade-panel border border-trade-border rounded-xl overflow-hidden">
                        <div className="p-5 border-b border-trade-border">
                            <h4 className="text-lg font-semibold text-white">Capital Growth</h4>
                        </div>
                        <div className="p-5 space-y-3">
                            <div className="flex justify-between items-center">
                                <span className="text-trade-muted">Initial Capital</span>
                                <span className="text-white font-medium">{formatCurrency(result.initialCapital)}</span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-trade-muted">Final Capital</span>
                                <span className="text-white font-medium">{formatCurrency(result.finalCapital)}</span>
                            </div>
                            <div className="h-2 bg-trade-bg rounded-full overflow-hidden">
                                <div
                                    className={`h-full transition-all ${
                                        result.returnPercent >= 0 ? 'bg-emerald-500' : 'bg-red-500'
                                    }`}
                                    style={{
                                        width: `${Math.min(Math.abs(result.returnPercent), 100)}%`
                                    }}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Trade List */}
                    {result.trades && result.trades.length > 0 && (
                        <div className="bg-trade-panel border border-trade-border rounded-xl overflow-hidden">
                            <div className="p-5 border-b border-trade-border">
                                <h4 className="text-lg font-semibold text-white">Trade History</h4>
                            </div>
                            <div className="overflow-x-auto">
                                <table className="w-full text-sm">
                                    <thead className="bg-trade-bg text-trade-muted border-b border-trade-border">
                                    <tr>
                                        <th className="px-4 py-3 text-left">#</th>
                                        <th className="px-4 py-3 text-left">Entry Time</th>
                                        <th className="px-4 py-3 text-left">Entry Price</th>
                                        <th className="px-4 py-3 text-left">Exit Price</th>
                                        <th className="px-4 py-3 text-left">Quantity</th>
                                        <th className="px-4 py-3 text-right">P&L</th>
                                    </tr>
                                    </thead>
                                    <tbody className="divide-y divide-trade-border">
                                    {result.trades.slice(0, 10).map((trade, idx) => (
                                        <tr key={idx} className="hover:bg-trade-bg/50">
                                            <td className="px-4 py-3 text-white">{idx + 1}</td>
                                            <td className="px-4 py-3 text-trade-muted">
                                                {new Date(trade.entryTime).toLocaleString()}
                                            </td>
                                            <td className="px-4 py-3 text-white">₹{trade.entryPrice.toFixed(2)}</td>
                                            <td className="px-4 py-3 text-white">₹{trade.exitPrice.toFixed(2)}</td>
                                            <td className="px-4 py-3 text-white">{trade.quantity}</td>
                                            <td className={`px-4 py-3 text-right font-medium ${
                                                trade.pnl >= 0 ? 'text-emerald-400' : 'text-red-400'
                                            }`}>
                                                {trade.pnl >= 0 ? '+' : ''}{formatCurrency(trade.pnl)}
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default Backtester;