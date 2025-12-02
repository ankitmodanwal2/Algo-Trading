import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import api from '../../lib/api';
import toast from 'react-hot-toast';

const OrderForm = () => {
    const { register, handleSubmit, reset } = useForm();
    const [loading, setLoading] = useState(false);

    const onSubmit = async (data) => {
        setLoading(true);
        try {
            await api.post('/orders/place', {
                ...data,
                brokerAccountId: 1, // Hardcoded for now, should come from selector
                orderType: 'LIMIT', // Simplified for UI
                side: data.side
            });
            toast.success('Order Placed Successfully');
            reset();
        } catch (err) {
            toast.error('Failed to place order');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-trade-panel border border-trade-border rounded-xl p-6">
            <h3 className="text-lg font-semibold mb-4 text-trade-text">Place Order</h3>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

                {/* Symbol Input */}
                <div>
                    <label className="block text-sm text-trade-muted mb-1">Symbol</label>
                    <input
                        {...register('symbol', { required: true })}
                        className="w-full bg-trade-bg border border-trade-border rounded p-2 text-white focus:outline-none focus:border-trade-primary"
                        placeholder="e.g. SBIN-EQ"
                    />
                </div>

                {/* Side Selection */}
                <div className="grid grid-cols-2 gap-4">
                    <label className="cursor-pointer">
                        <input type="radio" value="BUY" {...register('side')} className="peer sr-only" defaultChecked />
                        <div className="text-center py-2 rounded border border-trade-border peer-checked:bg-trade-buy peer-checked:text-white peer-checked:border-transparent hover:bg-trade-bg transition-all">
                            BUY
                        </div>
                    </label>
                    <label className="cursor-pointer">
                        <input type="radio" value="SELL" {...register('side')} className="peer sr-only" />
                        <div className="text-center py-2 rounded border border-trade-border peer-checked:bg-trade-sell peer-checked:text-white peer-checked:border-transparent hover:bg-trade-bg transition-all">
                            SELL
                        </div>
                    </label>
                </div>

                {/* Quantity & Price */}
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm text-trade-muted mb-1">Qty</label>
                        <input type="number" {...register('quantity')} className="w-full bg-trade-bg border border-trade-border rounded p-2 text-white" />
                    </div>
                    <div>
                        <label className="block text-sm text-trade-muted mb-1">Price</label>
                        <input type="number" step="0.05" {...register('price')} className="w-full bg-trade-bg border border-trade-border rounded p-2 text-white" />
                    </div>
                </div>

                <button
                    disabled={loading}
                    className="w-full bg-trade-primary hover:bg-blue-600 text-white font-bold py-3 rounded transition-colors disabled:opacity-50"
                >
                    {loading ? 'Processing...' : 'Execute Order'}
                </button>
            </form>
        </div>
    );
};

export default OrderForm;