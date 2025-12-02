import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/useAuthStore';
import { Lock, User, ArrowRight, Activity, TrendingUp } from 'lucide-react';
import toast from 'react-hot-toast';

const Login = () => {
    const { register, handleSubmit, formState: { errors } } = useForm();
    const login = useAuthStore((state) => state.login);
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    const onSubmit = async (data) => {
        setIsLoading(true);
        // Debug log to check Origin
        console.log("Login attempt from origin:", window.location.origin);

        const success = await login(data.username, data.password);

        if (success) {
            toast.success('Access Granted', {
                style: { background: '#10b981', color: '#fff' },
                iconTheme: { primary: '#fff', secondary: '#10b981' },
            });
            navigate('/');
        } else {
            toast.error('Connection Refused. Check console for CORS/Network errors.', {
                style: { background: '#ef4444', color: '#fff' },
            });
        }
        setIsLoading(false);
    };

    return (
        <div className="min-h-screen flex bg-trade-bg font-sans">
            {/* Left Panel - Brand & Visuals (Hidden on Mobile) */}
            <div className="hidden lg:flex w-1/2 bg-trade-panel relative overflow-hidden items-center justify-center border-r border-trade-border">
                {/* Ambient Background Effects */}
                <div className="absolute top-0 left-0 w-full h-full bg-gradient-to-br from-blue-900/20 to-trade-bg z-0"></div>
                <div className="absolute w-96 h-96 bg-blue-500/10 rounded-full blur-[100px] -top-20 -left-20 animate-pulse"></div>
                <div className="absolute w-96 h-96 bg-purple-500/10 rounded-full blur-[100px] bottom-0 right-0"></div>

                <div className="relative z-10 p-12 text-white/90 max-w-xl">
                    <div className="mb-8 flex items-center gap-4">
                        <div className="p-3 bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl shadow-lg shadow-blue-500/20">
                            <Activity size={32} className="text-white" />
                        </div>
                        <h1 className="text-3xl font-bold tracking-tight text-white">AlgoTrade Pro</h1>
                    </div>

                    <h2 className="text-5xl font-extrabold mb-6 leading-tight">
                        Institutional Grade <br/>
                        <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-300">
              Execution Logic
            </span>
                    </h2>

                    <p className="text-lg text-slate-400 leading-relaxed mb-10 border-l-4 border-blue-500/50 pl-6">
                        Access real-time market data, automated strategies, and low-latency execution in a unified dashboard.
                    </p>

                    <div className="grid grid-cols-2 gap-6">
                        <div className="p-5 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md">
                            <TrendingUp className="text-green-400 mb-3" size={24} />
                            <div className="text-3xl font-bold text-white">99.9%</div>
                            <div className="text-sm text-slate-400 font-medium">System Uptime</div>
                        </div>
                        <div className="p-5 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md">
                            <Lock className="text-blue-400 mb-3" size={24} />
                            <div className="text-3xl font-bold text-white">AES-256</div>
                            <div className="text-sm text-slate-400 font-medium">End-to-End Encrypted</div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Right Panel - Login Form */}
            <div className="w-full lg:w-1/2 flex items-center justify-center p-8 relative bg-trade-bg">
                <div className="max-w-[420px] w-full">
                    {/* Mobile Header */}
                    <div className="text-center mb-10 lg:hidden">
                        <div className="flex justify-center mb-4">
                            <div className="p-3 bg-blue-600 rounded-xl">
                                <Activity size={24} className="text-white" />
                            </div>
                        </div>
                        <h2 className="text-2xl font-bold text-white">AlgoTrade</h2>
                    </div>

                    <div className="mb-10">
                        <h3 className="text-3xl font-bold text-white mb-3">Welcome Back</h3>
                        <p className="text-slate-400">Enter your credentials to access the trading terminal.</p>
                    </div>

                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                        {/* Username Input */}
                        <div className="space-y-2">
                            <label className="text-sm font-semibold text-slate-300 ml-1">Username</label>
                            <div className="relative group">
                                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500 group-focus-within:text-blue-400 transition-colors">
                                    <User size={20} />
                                </div>
                                <input
                                    {...register('username', { required: 'Username is required' })}
                                    className="w-full pl-11 pr-4 py-4 bg-trade-panel border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all font-medium"
                                    placeholder="Enter your user ID"
                                    autoComplete="off"
                                />
                            </div>
                            {errors.username && <p className="text-red-400 text-xs ml-1 font-medium">{errors.username.message}</p>}
                        </div>

                        {/* Password Input */}
                        <div className="space-y-2">
                            <label className="text-sm font-semibold text-slate-300 ml-1">Password</label>
                            <div className="relative group">
                                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500 group-focus-within:text-blue-400 transition-colors">
                                    <Lock size={20} />
                                </div>
                                <input
                                    type="password"
                                    {...register('password', { required: 'Password is required' })}
                                    className="w-full pl-11 pr-4 py-4 bg-trade-panel border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all font-medium"
                                    placeholder="••••••••"
                                />
                            </div>
                            {errors.password && <p className="text-red-400 text-xs ml-1 font-medium">{errors.password.message}</p>}
                        </div>

                        <div className="flex items-center justify-between text-sm pt-2">
                            <label className="flex items-center gap-2 cursor-pointer text-slate-400 hover:text-slate-300 transition-colors">
                                <input
                                    type="checkbox"
                                    className="w-4 h-4 rounded border-slate-600 bg-trade-panel text-blue-500 focus:ring-offset-0 focus:ring-blue-500/20"
                                />
                                Remember me
                            </label>
                            <a href="#" className="text-blue-400 hover:text-blue-300 font-medium transition-colors">Forgot password?</a>
                        </div>

                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-500 hover:to-blue-400 text-white font-bold py-4 rounded-xl transition-all transform active:scale-[0.98] shadow-lg shadow-blue-500/25 flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed mt-4"
                        >
                            {isLoading ? (
                                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                            ) : (
                                <>
                                    Sign In to Terminal <ArrowRight size={18} />
                                </>
                            )}
                        </button>
                    </form>

                    <p className="mt-8 text-center text-sm text-slate-500">
                        Protected by reCAPTCHA and subject to the
                        <a href="#" className="text-slate-400 hover:text-white mx-1 underline">Privacy Policy</a>
                        and
                        <a href="#" className="text-slate-400 hover:text-white mx-1 underline">Terms of Service</a>.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Login;