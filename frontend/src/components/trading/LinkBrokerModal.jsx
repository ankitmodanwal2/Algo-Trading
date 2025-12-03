import React, { useState } from 'react';
import { X, AlertTriangle } from 'lucide-react';
import { useForm } from 'react-hook-form';
import api from '../../lib/api';
import toast from 'react-hot-toast';

const LinkBrokerModal = ({ isOpen, onClose, onSuccess }) => {
    const [selectedBroker, setSelectedBroker] = useState('angelone');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { register, handleSubmit, reset } = useForm();

    if (!isOpen) return null;

    const onSubmit = async (data) => {
        setIsSubmitting(true);
        try {
            // 1. Construct Credentials JSON
            const credentials = {
                apiKey: data.apiKey,
                clientCode: data.clientCode,
                password: data.password,
                totpKey: data.totpKey
            };

            // 2. Construct Payload
            const payload = {
                brokerId: selectedBroker,
                metadataJson: JSON.stringify({ name: data.name || 'My Account' }),
                credentialsJson: JSON.stringify(credentials)
            };

            // 3. Send to Backend
            await api.post('/brokers/link', payload);

            toast.success('Broker Linked Successfully!');
            reset();
            onSuccess(); // Refresh parent list
            onClose();
        } catch (err) {
            // Handle the validation error returned by the backend
            const msg = err.response?.data?.message || 'Invalid Credentials. Please check API Key/TOTP.';
            toast.error(msg, { duration: 5000 });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="bg-trade-panel border border-trade-border rounded-xl w-full max-w-md shadow-2xl">

                {/* Header */}
                <div className="flex justify-between items-center p-6 border-b border-trade-border">
                    <h2 className="text-xl font-bold text-white">Link Broker Account</h2>
                    <button onClick={onClose} className="text-trade-muted hover:text-white transition-colors">
                        <X size={24} />
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-4">

                    {/* Broker Selector */}
                    <div>
                        <label className="block text-sm text-trade-muted mb-2">Select Broker</label>
                        <select
                            value={selectedBroker}
                            onChange={(e) => setSelectedBroker(e.target.value)}
                            className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white focus:border-trade-primary outline-none"
                        >
                            <option value="angelone">Angel One</option>
                            <option value="fyers">Fyers (Coming Soon)</option>
                            <option value="dhan">Dhan (Coming Soon)</option>
                        </select>
                    </div>

                    {/* Dynamic Fields for Angel One */}
                    {selectedBroker === 'angelone' && (
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm text-trade-muted mb-1">Account Name (Alias)</label>
                                <input {...register('name')} placeholder="e.g. My Primary Account" className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white" />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm text-trade-muted mb-1">Client ID</label>
                                    <input {...register('clientCode', { required: true })} placeholder="A12345" className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white" />
                                </div>
                                <div>
                                    <label className="block text-sm text-trade-muted mb-1">MPIN / Password</label>
                                    <input type="password" {...register('password', { required: true })} placeholder="****" className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white" />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm text-trade-muted mb-1">TOTP Secret (Base32)</label>
                                <input {...register('totpKey', { required: true })} placeholder="JBSWY3..." className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white" />
                                <p className="text-xs text-trade-muted mt-1">Scan QR code to get this secret key.</p>
                            </div>

                            <div>
                                <label className="block text-sm text-trade-muted mb-1">SmartAPI Key</label>
                                <input {...register('apiKey', { required: true })} placeholder="Long UUID String" className="w-full bg-trade-bg border border-trade-border rounded-lg p-3 text-white" />
                            </div>
                        </div>
                    )}

                    <div className="pt-4 flex gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-3 rounded-lg text-trade-muted hover:bg-trade-bg transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="flex-1 bg-trade-primary hover:bg-blue-600 text-white font-bold py-3 rounded-lg transition-colors disabled:opacity-50 flex justify-center items-center gap-2"
                        >
                            {isSubmitting ? <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" /> : 'Connect & Verify'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default LinkBrokerModal;