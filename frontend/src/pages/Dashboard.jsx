import React from 'react';
import MarketDataTicker from '../components/trading/MarketDataTicker';
import OrderForm from '../components/trading/OrderForm';
import PositionsTable from '../components/trading/PositionsTable'; // <--- Import

const Dashboard = () => {
    return (
        <div>
            <MarketDataTicker />

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
                {/* Left Column: Order Form */}
                <div className="lg:col-span-1">
                    <OrderForm />
                </div>

                {/* Right Column: Chart */}
                <div className="lg:col-span-2 bg-trade-panel border border-trade-border rounded-xl min-h-[400px] flex items-center justify-center text-trade-muted">
                    Chart Area (Placeholder)
                </div>
            </div>

            {/* Bottom Row: Positions */}
            <PositionsTable /> {/* <--- Use Component */}
        </div>
    );
};

export default Dashboard;