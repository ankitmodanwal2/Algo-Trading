import React from 'react';
import { Play, Pause, Settings } from 'lucide-react';

const Strategies = () => {
    return (
        <div className="space-y-6">
            <h2 className="text-2xl font-bold text-white">Active Strategies</h2>

            {/* Strategy List */}
            <div className="bg-trade-panel border border-trade-border rounded-xl overflow-hidden">
                <table className="w-full text-left text-sm">
                    <thead className="bg-trade-bg text-trade-muted uppercase font-medium border-b border-trade-border">
                    <tr>
                        <th className="px-6 py-4">Strategy Name</th>
                        <th className="px-6 py-4">Instrument</th>
                        <th className="px-6 py-4">Status</th>
                        <th className="px-6 py-4">P&L</th>
                        <th className="px-6 py-4">Actions</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-trade-border text-white">
                    <tr className="hover:bg-trade-bg/50 transition-colors">
                        <td className="px-6 py-4 font-medium">9:20 Straddle</td>
                        <td className="px-6 py-4 text-trade-muted">BANKNIFTY</td>
                        <td className="px-6 py-4"><span className="text-green-400">Running</span></td>
                        <td className="px-6 py-4 text-green-400">+₹1,250.00</td>
                        <td className="px-6 py-4 flex gap-3">
                            <button className="p-2 hover:bg-trade-border rounded text-yellow-400"><Pause size={16} /></button>
                            <button className="p-2 hover:bg-trade-border rounded text-blue-400"><Settings size={16} /></button>
                        </td>
                    </tr>
                    {/* Add more rows */}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default Strategies;