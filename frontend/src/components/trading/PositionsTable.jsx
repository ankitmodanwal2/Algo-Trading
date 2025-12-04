import React, { useEffect, useState } from 'react';
import api from '../../lib/api';
import { RefreshCw, AlertCircle } from 'lucide-react';

const PositionsTable = () => {
    const [positions, setPositions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [totalPnl, setTotalPnl] = useState(0);
    const [activeBrokerId, setActiveBrokerId] = useState(null);

    // 1. First, fetch the list of linked brokers to find a valid ID
    const fetchBrokerId = async () => {
        try {
            const res = await api.get('/brokers/linked');
            if (res.data && res.data.length > 0) {
                // Automatically pick the most recently added broker (last in the list)
                const newest = res.data[res.data.length - 1];
                setActiveBrokerId(newest.id);
            } else {
                setError("No brokers linked. Please link a broker first.");
            }
        } catch (err) {
            console.error("Failed to fetch linked brokers", err);
            setError("Failed to connect to backend service.");
        }
    };

    // Run once on mount to get the ID
    useEffect(() => {
        fetchBrokerId();
    }, []);

    // 2. Once we have an ID, fetch positions for it
    const fetchPositions = async () => {
        if (!activeBrokerId) return;

        // Avoid setting loading true on background refreshes to prevent flickering
        // Only show spinner on initial load
        if (positions.length === 0 && !error) setLoading(true);

        try {
            // Dynamic ID usage
            const res = await api.get(`/brokers/${activeBrokerId}/positions`);

            // SAFETY CHECK: Ensure data is an array before using it
            if (Array.isArray(res.data)) {
                setPositions(res.data);
                const total = res.data.reduce((sum, pos) => sum + (parseFloat(pos.pnl) || 0), 0);
                setTotalPnl(total);
                setError(null); // Clear errors on success
            } else {
                console.warn("API returned non-array data:", res.data);
                setPositions([]);
            }
        } catch (err) {
            console.error("Failed to fetch positions:", err);
            const msg = err.response?.data?.message || "Could not load positions. Is the broker connected?";
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    // Poll for positions whenever activeBrokerId changes
    useEffect(() => {
        if (activeBrokerId) {
            fetchPositions();
            const interval = setInterval(fetchPositions, 5000);
            return () => clearInterval(interval);
        }
    }, [activeBrokerId]);

    if (error) {
        return (
            <div className="bg-trade-panel border border-trade-border rounded-xl p-6 mt-8 flex items-center gap-3 text-red-400">
                <AlertCircle size={20} />
                <span>{error}</span>
                <button
                    onClick={() => window.location.reload()}
                    className="ml-auto text-xs bg-white/10 hover:bg-white/20 px-3 py-1 rounded text-white transition-colors"
                >
                    Retry
                </button>
            </div>
        );
    }

    return (
        <div className="bg-trade-panel border border-trade-border rounded-xl overflow-hidden mt-8">
            <div className="px-6 py-4 border-b border-trade-border flex justify-between items-center">
                <div className="flex items-center gap-4">
                    <h3 className="font-semibold text-white">Open Positions</h3>
                    {loading && <RefreshCw size={16} className="text-trade-primary animate-spin" />}
                </div>

                <span className="text-sm text-trade-muted">
                  Total P&L:
                  <span className={`ml-2 font-bold text-lg ${totalPnl >= 0 ? 'text-trade-buy' : 'text-trade-sell'}`}>
                    {totalPnl >= 0 ? '+' : ''}₹{totalPnl.toFixed(2)}
                  </span>
                </span>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                    <thead className="bg-trade-bg text-trade-muted border-b border-trade-border">
                    <tr>
                        <th className="px-6 py-3 font-medium">Instrument</th>
                        <th className="px-6 py-3 font-medium">Product</th>
                        <th className="px-6 py-3 font-medium">Net Qty</th>
                        <th className="px-6 py-3 font-medium">Avg. Price</th>
                        <th className="px-6 py-3 font-medium">LTP</th>
                        <th className="px-6 py-3 font-medium">P&L</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-trade-border text-white">
                    {positions.length === 0 ? (
                        <tr>
                            <td colSpan="6" className="text-center py-8 text-trade-muted">
                                {loading ? "Loading positions..." : "No open positions found."}
                            </td>
                        </tr>
                    ) : (
                        positions.map((pos, index) => {
                            const pnl = parseFloat(pos.pnl) || 0;
                            const isProfit = pnl >= 0;
                            return (
                                <tr key={index} className="hover:bg-trade-bg/50 transition-colors">
                                    <td className="px-6 py-3 font-medium text-white">{pos.symbol}</td>
                                    <td className="px-6 py-3 text-xs uppercase text-trade-muted">{pos.productType}</td>
                                    <td className={`px-6 py-3 font-bold ${pos.netQuantity > 0 ? 'text-blue-400' : 'text-orange-400'}`}>
                                        {pos.netQuantity}
                                    </td>
                                    <td className="px-6 py-3">₹{pos.avgPrice}</td>
                                    <td className="px-6 py-3">₹{pos.ltp}</td>
                                    <td className={`px-6 py-3 font-bold ${isProfit ? 'text-trade-buy' : 'text-trade-sell'}`}>
                                        {isProfit ? '+' : ''}{pnl.toFixed(2)}
                                    </td>
                                </tr>
                            );
                        })
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default PositionsTable;