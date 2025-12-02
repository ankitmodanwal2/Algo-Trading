import React from 'react';

const Settings = () => {
    return (
        <div className="max-w-2xl">
            <h2 className="text-2xl font-bold text-white mb-6">System Settings</h2>
            <div className="bg-trade-panel border border-trade-border rounded-xl p-6 space-y-6">
                <div>
                    <label className="block text-sm text-trade-muted mb-2">Default Order Quantity</label>
                    <input type="number" className="w-full bg-trade-bg border border-trade-border rounded p-3 text-white" defaultValue={1} />
                </div>
                <div>
                    <label className="block text-sm text-trade-muted mb-2">Risk per Trade (%)</label>
                    <input type="number" className="w-full bg-trade-bg border border-trade-border rounded p-3 text-white" defaultValue={2} />
                </div>
                <button className="bg-trade-primary text-white px-6 py-2 rounded-lg font-medium">Save Changes</button>
            </div>
        </div>
    );
};

export default Settings;