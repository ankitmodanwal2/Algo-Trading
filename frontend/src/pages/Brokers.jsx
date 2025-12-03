import React, { useEffect, useState } from 'react';
import { Plus, Trash2, RefreshCw } from 'lucide-react';
import api from '../lib/api';
import LinkBrokerModal from '../components/trading/LinkBrokerModal';
import toast from 'react-hot-toast';

const Brokers = () => {
    const [brokers, setBrokers] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(false);

    const fetchBrokers = async () => {
        setLoading(true);
        try {
            const res = await api.get('/brokers/linked');
            setBrokers(res.data);
        } catch (err) {
            console.error("Failed to load brokers", err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchBrokers();
    }, []);

    const handleDelete = async (id) => {
        if(!window.confirm("Are you sure you want to unlink this broker?")) return;
        try {
            await api.delete(`/brokers/${id}`);
            toast.success("Broker unlinked");
            fetchBrokers();
        } catch(e) {
            toast.error("Failed to unlink");
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold text-white">Connected Brokers</h2>
                <button
                    onClick={() => setIsModalOpen(true)}
                    className="flex items-center gap-2 bg-trade-primary hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition-colors"
                >
                    <Plus size={18} /> Link Broker
                </button>
            </div>

            {loading && brokers.length === 0 && <p className="text-trade-muted">Loading accounts...</p>}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {brokers.map((broker) => {
                    let meta = {};
                    try {
                        meta = JSON.parse(broker.metadataJson || '{}');
                    } catch(e) { meta = { name: 'Unknown' }; }

                    return (
                        <div key={broker.id} className="bg-trade-panel border border-trade-border rounded-xl p-6 relative group hover:border-trade-primary/50 transition-colors">
                            <div className="flex justify-between items-start mb-4">
                                <div className="p-3 bg-trade-bg rounded-lg border border-trade-border">
                                    <span className="font-bold text-blue-400 uppercase">{broker.brokerId.substring(0, 2)}</span>
                                </div>
                                <span className="px-2 py-1 rounded text-xs font-medium bg-green-500/10 text-green-400">
                  CONNECTED
                </span>
                            </div>

                            <h3 className="text-lg font-semibold text-white mb-1">{meta.name || 'Trading Account'}</h3>
                            <p className="text-sm text-trade-muted mb-4 capitalize">ID: {broker.id} • {broker.brokerId}</p>

                            <div className="flex items-center justify-between text-sm text-trade-muted pt-4 border-t border-trade-border">
                                <span>Linked: {new Date(broker.createdAt).toLocaleDateString()}</span>
                                <button
                                    onClick={() => handleDelete(broker.id)}
                                    className="text-red-400 hover:text-red-300 transition-colors p-2 hover:bg-white/5 rounded"
                                >
                                    <Trash2 size={18} />
                                </button>
                            </div>
                        </div>
                    );
                })}
            </div>

            <LinkBrokerModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSuccess={fetchBrokers}
            />
        </div>
    );
};

export default Brokers;