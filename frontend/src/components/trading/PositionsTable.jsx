import React from 'react';

const PositionsTable = () => {
    // Mock data - connect to API /orders later
    const positions = [
        { id: 1, symbol: 'SBIN-EQ', qty: 50, avg: 540.50, ltp: 542.10, pnl: 80.00 },
        { id: 2, symbol: 'IDEA-EQ', qty: 1000, avg: 14.20, ltp: 14.10, pnl: -100.00 },
    ];

    return (
        <div className="bg-trade-panel border border-trade-border rounded-xl overflow-hidden mt-8">
            <div className="px-6 py-4 border-b border-trade-border flex justify-between items-center">
                <h3 className="font-semibold text-white">Open Positions</h3>
                <span className="text-sm text-trade-muted">Total P&L: <span className="text-red-400 font-bold">-₹20.00</span></span>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                    <thead className="bg-trade-bg text-trade-muted border-b border-trade-border">
                    <tr>
                        <th className="px-6 py-3 font-medium">Instrument</th>
                        <th className="px-6 py-3 font-medium">Qty</th>
                        <th className="px-6 py-3 font-medium">Avg. Price</th>
                        <th className="px-6 py-3 font-medium">LTP</th>
                        <th className="px-6 py-3 font-medium">P&L</th>
                        <th className="px-6 py-3 font-medium text-right">Action</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-trade-border text-white">
                    {positions.map((pos) => {
                        const isProfit = pos.pnl >= 0;
                        return (
                            <tr key={pos.id} className="hover:bg-trade-bg/50 transition-colors">
                                <td className="px-6 py-3 font-medium">{pos.symbol}</td>
                                <td className="px-6 py-3">{pos.qty}</td>
                                <td className="px-6 py-3">₹{pos.avg}</td>
                                <td className="px-6 py-3">₹{pos.ltp}</td>
                                <td className={`px-6 py-3 font-bold ${isProfit ? 'text-trade-buy' : 'text-trade-sell'}`}>
                                    {isProfit ? '+' : ''}{pos.pnl}
                                </td>
                                <td className="px-6 py-3 text-right">
                                    <button className="text-xs bg-trade-border hover:bg-white/10 px-3 py-1 rounded transition-colors text-white">
                                        Exit
                                    </button>
                                </td>
                            </tr>
                        );
                    })}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default PositionsTable;