import React, { useState } from 'react';
import { Plus, Trash2, CheckCircle, XCircle } from 'lucide-react';

const Brokers = () => {
    // Mock data for now - hook this up to GET /api/v1/brokers/linked later
    const [brokers] = useState([
        { id: 1, name: 'Angel One - Main', brokerId: 'angelone', status: 'ACTIVE', lastSync: '1 min ago' },
    ]);

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold text-white">Connected Brokers</h2>
                <button className="flex items-center gap-2 bg-trade-primary hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition-colors">
                    <Plus size={18} /> Link Broker
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {brokers.map((broker) => (
                    <div key={broker.id} className="bg-trade-panel border border-trade-border rounded-xl p-6 relative group">
                        <div className="flex justify-between items-start mb-4">
                            <div className="p-3 bg-trade-bg rounded-lg border border-trade-border">
                                {/* Broker Logo Placeholder */}
                                <span className="font-bold text-blue-400">A1</span>
                            </div>
                            <span className={`px-2 py-1 rounded text-xs font-medium ${broker.status === 'ACTIVE' ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'}`}>
                {broker.status}
              </span>
                        </div>

                        <h3 className="text-lg font-semibold text-white mb-1">{broker.name}</h3>
                        <p className="text-sm text-trade-muted mb-4 capitalize">{broker.brokerId}</p>

                        <div className="flex items-center justify-between text-sm text-trade-muted pt-4 border-t border-trade-border">
                            <span>Synced: {broker.lastSync}</span>
                            <button className="text-red-400 hover:text-red-300 transition-colors opacity-0 group-hover:opacity-100">
                                <Trash2 size={18} />
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Brokers;