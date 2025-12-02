import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { TrendingUp, TrendingDown, Activity } from 'lucide-react';

const MarketDataTicker = () => {
    const [ticks, setTicks] = useState({});
    const [connectionStatus, setStatus] = useState('CONNECTING');

    useEffect(() => {
        const client = new Client({
            brokerURL: 'ws://localhost:8080/ws',
            reconnectDelay: 5000,
            onConnect: () => {
                setStatus('CONNECTED');
                // Subscribe to a few major indices/stocks
                ['NSE:3045', 'NSE:2885', 'NSE:11536'].forEach(token => {
                    client.subscribe(`/topic/market/${token}`, (msg) => {
                        const tick = JSON.parse(msg.body);
                        setTicks(prev => ({ ...prev, [tick.instrumentToken]: tick }));
                    });
                });
            },
            onDisconnect: () => setStatus('DISCONNECTED')
        });

        client.activate();
        return () => client.deactivate();
    }, []);

    return (
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4 mb-8">
            {/* Connection Status Card */}
            <div className="bg-trade-panel border border-trade-border p-4 rounded-xl flex items-center justify-between">
                <div>
                    <p className="text-trade-muted text-sm">Feed Status</p>
                    <p className={`font-bold ${connectionStatus === 'CONNECTED' ? 'text-green-400' : 'text-red-400'}`}>
                        {connectionStatus}
                    </p>
                </div>
                <Activity className="text-trade-muted" />
            </div>

            {/* Render Ticks */}
            {Object.values(ticks).map((tick) => {
                const isPositive = tick.change >= 0; // Assuming backend sends change, else calculate locally
                return (
                    <div key={tick.instrumentToken} className="bg-trade-panel border border-trade-border p-4 rounded-xl">
                        <div className="flex justify-between items-start mb-2">
                            <span className="text-trade-muted text-sm font-medium">{tick.instrumentToken}</span>
                            {isPositive ? <TrendingUp size={16} className="text-trade-buy" /> : <TrendingDown size={16} className="text-trade-sell" />}
                        </div>
                        <div className="flex items-baseline gap-2">
                            <span className="text-2xl font-bold text-trade-text">₹{tick.lastPrice}</span>
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default MarketDataTicker;