import React, { useState, useEffect } from 'react';
import { Play, Pause, Trash2, Plus, Settings, TrendingUp, Activity, Target, AlertCircle, Copy, Edit2 } from 'lucide-react';
import api from '../../lib/api';
import toast from 'react-hot-toast';

const StrategyBuilder = () => {
    const [strategies, setStrategies] = useState([]);
    const [showBuilder, setShowBuilder] = useState(false);
    const [editingStrategy, setEditingStrategy] = useState(null);
    const [activeTab, setActiveTab] = useState('my-strategies');

    // Pre-built strategy templates
    const strategyTemplates = [
        {
            id: 'sma_crossover',
            name: 'SMA Crossover',
            description: 'Buy when fast SMA crosses above slow SMA, sell on reverse',
            category: 'Trend Following',
            complexity: 'Beginner',
            params: {
                fastSMA: 9,
                slowSMA: 21,
                symbol: 'NSE:RELIANCE',
                quantity: 1,
                stopLoss: 2,
                target: 4
            }
        },
        {
            id: 'rsi_reversal',
            name: 'RSI Reversal',
            description: 'Buy when RSI drops below 30, sell when above 70',
            category: 'Mean Reversion',
            complexity: 'Beginner',
            params: {
                rsiPeriod: 14,
                oversoldLevel: 30,
                overboughtLevel: 70,
                symbol: 'NSE:TCS',
                quantity: 1,
                stopLoss: 2,
                target: 3
            }
        },
        {
            id: 'breakout',
            name: 'Breakout Strategy',
            description: 'Enter when price breaks above resistance with volume',
            category: 'Breakout',
            complexity: 'Intermediate',
            params: {
                lookbackPeriod: 20,
                volumeMultiplier: 1.5,
                symbol: 'NSE:INFY',
                quantity: 1,
                stopLoss: 1.5,
                target: 3
            }
        },
        {
            id: 'macd_momentum',
            name: 'MACD Momentum',
            description: 'Trade based on MACD line crossing signal line',
            category: 'Momentum',
            complexity: 'Intermediate',
            params: {
                fastEMA: 12,
                slowEMA: 26,
                signalSMA: 9,
                symbol: 'NSE:HDFCBANK',
                quantity: 1,
                stopLoss: 2,
                target: 4
            }
        },
        {
            id: 'bollinger_squeeze',
            name: 'Bollinger Squeeze',
            description: 'Trade volatility breakouts from Bollinger Band squeeze',
            category: 'Volatility',
            complexity: 'Advanced',
            params: {
                bbPeriod: 20,
                bbStdDev: 2,
                squeezeThreshold: 0.05,
                symbol: 'NSE:WIPRO',
                quantity: 1,
                stopLoss: 1.5,
                target: 3
            }
        },
        {
            id: 'opening_range',
            name: '9:20 Opening Range',
            description: 'Trade breakout from first 20 minutes range',
            category: 'Intraday',
            complexity: 'Beginner',
            params: {
                openingMinutes: 20,
                breakoutPercent: 0.5,
                symbol: 'NSE:BANKNIFTY',
                quantity: 1,
                stopLoss: 1,
                target: 2
            }
        }
    ];

    useEffect(() => {
        fetchStrategies();
    }, []);

    const fetchStrategies = async () => {
        try {
            const res = await api.get('/strategies');
            setStrategies(res.data || []);
        } catch (err) {
            console.error('Failed to fetch strategies', err);
        }
    };

    const createFromTemplate = async (template) => {
        try {
            const res = await api.post('/strategies', {
                name: template.name,
                templateId: template.id,
                description: template.description,
                paramsJson: JSON.stringify(template.params), // ✅ Must be paramsJson
                active: false
            });
            toast.success('Strategy created from template!');
            fetchStrategies();
            setActiveTab('my-strategies');
        } catch (err) {
            console.error('Create strategy error:', err);
            toast.error('Failed to create strategy: ' + (err.response?.data?.message || err.message));
        }
    };

    const toggleStrategy = async (id, currentStatus) => {
        try {
            const response = await api.patch(`/strategies/${id}`, { active: !currentStatus });
            console.log('Toggle response:', response.data);
            toast.success(currentStatus ? 'Strategy stopped' : 'Strategy started');
            fetchStrategies();
        } catch (err) {
            console.error('Toggle error:', err);
            const errorMsg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to toggle strategy';
            toast.error(errorMsg);
        }
    };

    const deleteStrategy = async (id) => {
        if (!window.confirm('Are you sure you want to delete this strategy?')) return;
        try {
            await api.delete(`/strategies/${id}`);
            toast.success('Strategy deleted');
            fetchStrategies();
        } catch (err) {
            toast.error('Failed to delete strategy');
        }
    };

    const cloneStrategy = async (strategy) => {
        try {
            await api.post('/strategies', {
                ...strategy,
                name: `${strategy.name} (Copy)`,
                active: false
            });
            toast.success('Strategy cloned!');
            fetchStrategies();
        } catch (err) {
            toast.error('Failed to clone strategy');
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold text-white">Trading Strategies</h2>
                    <p className="text-trade-muted mt-1">Automate your trading with powerful strategies</p>
                </div>
                <button
                    onClick={() => setShowBuilder(true)}
                    className="flex items-center gap-2 bg-trade-primary hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition-colors font-medium"
                >
                    <Plus size={18} />
                    Create Custom Strategy
                </button>
            </div>

            {/* Tabs */}
            <div className="flex gap-4 border-b border-trade-border">
                <button
                    onClick={() => setActiveTab('my-strategies')}
                    className={`px-4 py-2 font-medium transition-colors border-b-2 ${
                        activeTab === 'my-strategies'
                            ? 'border-trade-primary text-white'
                            : 'border-transparent text-trade-muted hover:text-white'
                    }`}
                >
                    My Strategies ({strategies.length})
                </button>
                <button
                    onClick={() => setActiveTab('templates')}
                    className={`px-4 py-2 font-medium transition-colors border-b-2 ${
                        activeTab === 'templates'
                            ? 'border-trade-primary text-white'
                            : 'border-transparent text-trade-muted hover:text-white'
                    }`}
                >
                    Strategy Templates ({strategyTemplates.length})
                </button>
            </div>

            {/* My Strategies Tab */}
            {activeTab === 'my-strategies' && (
                <div className="space-y-4">
                    {strategies.length === 0 ? (
                        <div className="bg-trade-panel border border-trade-border rounded-xl p-12 text-center">
                            <Activity size={48} className="mx-auto text-trade-muted mb-4" />
                            <h3 className="text-lg font-semibold text-white mb-2">No strategies yet</h3>
                            <p className="text-trade-muted mb-6">
                                Create your first strategy or use a template to get started
                            </p>
                            <button
                                onClick={() => setActiveTab('templates')}
                                className="bg-trade-primary hover:bg-blue-600 text-white px-6 py-2 rounded-lg transition-colors"
                            >
                                Browse Templates
                            </button>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            {strategies.map(strategy => (
                                <div key={strategy.id} className="bg-trade-panel border border-trade-border rounded-xl p-5 hover:border-trade-primary/50 transition-colors">
                                    <div className="flex justify-between items-start mb-4">
                                        <div className="flex-1">
                                            <h3 className="text-lg font-semibold text-white mb-1">{strategy.name}</h3>
                                            <p className="text-sm text-trade-muted line-clamp-2">{strategy.description}</p>
                                        </div>
                                        <div className={`px-2 py-1 rounded text-xs font-bold ${
                                            strategy.active
                                                ? 'bg-emerald-500/20 text-emerald-400'
                                                : 'bg-slate-500/20 text-slate-400'
                                        }`}>
                                            {strategy.active ? 'ACTIVE' : 'INACTIVE'}
                                        </div>
                                    </div>

                                    {/* Stats */}
                                    <div className="grid grid-cols-3 gap-3 mb-4 text-sm">
                                        <div>
                                            <span className="text-trade-muted block">Trades</span>
                                            <span className="text-white font-medium">{strategy.totalTrades || 0}</span>
                                        </div>
                                        <div>
                                            <span className="text-trade-muted block">Win Rate</span>
                                            <span className="text-emerald-400 font-medium">
                                                {strategy.winRate || 0}%
                                            </span>
                                        </div>
                                        <div>
                                            <span className="text-trade-muted block">P&L</span>
                                            <span className={`font-medium ${
                                                (strategy.totalPnL || 0) >= 0 ? 'text-emerald-400' : 'text-red-400'
                                            }`}>
                                                {(strategy.totalPnL || 0) >= 0 ? '+' : ''}₹{(strategy.totalPnL || 0).toFixed(0)}
                                            </span>
                                        </div>
                                    </div>

                                    {/* Actions */}
                                    <div className="flex gap-2">
                                        <button
                                            onClick={() => toggleStrategy(strategy.id, strategy.active)}
                                            className={`flex-1 flex items-center justify-center gap-2 px-3 py-2 rounded-lg font-medium transition-colors ${
                                                strategy.active
                                                    ? 'bg-red-500/20 text-red-400 hover:bg-red-500/30'
                                                    : 'bg-emerald-500/20 text-emerald-400 hover:bg-emerald-500/30'
                                            }`}
                                        >
                                            {strategy.active ? <Pause size={16} /> : <Play size={16} />}
                                            {strategy.active ? 'Stop' : 'Start'}
                                        </button>
                                        <button
                                            onClick={() => cloneStrategy(strategy)}
                                            className="p-2 rounded-lg bg-trade-bg hover:bg-trade-border text-trade-muted hover:text-white transition-colors"
                                        >
                                            <Copy size={16} />
                                        </button>
                                        <button
                                            onClick={() => setEditingStrategy(strategy)}
                                            className="p-2 rounded-lg bg-trade-bg hover:bg-trade-border text-trade-muted hover:text-white transition-colors"
                                        >
                                            <Edit2 size={16} />
                                        </button>
                                        <button
                                            onClick={() => deleteStrategy(strategy.id)}
                                            className="p-2 rounded-lg bg-trade-bg hover:bg-red-500/20 text-trade-muted hover:text-red-400 transition-colors"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}

            {/* Templates Tab */}
            {activeTab === 'templates' && (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {strategyTemplates.map(template => (
                        <div key={template.id} className="bg-trade-panel border border-trade-border rounded-xl p-5 hover:border-trade-primary/50 transition-colors">
                            <div className="flex items-start justify-between mb-3">
                                <div className="flex-1">
                                    <h3 className="text-lg font-semibold text-white mb-1">{template.name}</h3>
                                    <p className="text-xs text-trade-muted mb-2">{template.category}</p>
                                </div>
                                <span className={`px-2 py-1 rounded text-xs font-medium ${
                                    template.complexity === 'Beginner' ? 'bg-emerald-500/20 text-emerald-400' :
                                        template.complexity === 'Intermediate' ? 'bg-amber-500/20 text-amber-400' :
                                            'bg-red-500/20 text-red-400'
                                }`}>
                                    {template.complexity}
                                </span>
                            </div>

                            <p className="text-sm text-trade-muted mb-4 line-clamp-2">{template.description}</p>

                            {/* Key Parameters */}
                            <div className="bg-trade-bg rounded-lg p-3 mb-4 space-y-1 text-xs">
                                {Object.entries(template.params).slice(0, 3).map(([key, value]) => (
                                    <div key={key} className="flex justify-between">
                                        <span className="text-trade-muted capitalize">{key.replace(/([A-Z])/g, ' $1')}</span>
                                        <span className="text-white font-medium">{value}</span>
                                    </div>
                                ))}
                            </div>

                            <button
                                onClick={() => createFromTemplate(template)}
                                className="w-full bg-trade-primary hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition-colors font-medium flex items-center justify-center gap-2"
                            >
                                <Plus size={16} />
                                Use Template
                            </button>
                        </div>
                    ))}
                </div>
            )}

            {/* Strategy Info Banner */}
            <div className="bg-gradient-to-r from-blue-500/10 to-purple-500/10 border border-blue-500/20 rounded-xl p-6">
                <div className="flex items-start gap-4">
                    <div className="p-3 bg-blue-500/20 rounded-lg">
                        <AlertCircle className="text-blue-400" size={24} />
                    </div>
                    <div>
                        <h3 className="text-lg font-semibold text-white mb-2">Strategy Guidelines</h3>
                        <ul className="text-sm text-trade-muted space-y-1">
                            <li>• Always backtest strategies before deploying with real capital</li>
                            <li>• Monitor active strategies regularly and adjust parameters as needed</li>
                            <li>• Use appropriate position sizing and risk management</li>
                            <li>• Consider market conditions and volatility when activating strategies</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default StrategyBuilder;