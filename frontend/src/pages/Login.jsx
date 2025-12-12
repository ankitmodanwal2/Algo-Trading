import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/useAuthStore';
import { Lock, User, ArrowRight } from 'lucide-react';
import toast from 'react-hot-toast';

const Login = () => {
    const { register, handleSubmit } = useForm();
    const login = useAuthStore((state) => state.login);
    const [isLoading, setIsLoading] = useState(false);

    // 1. Force Clear on Mount
    useEffect(() => {
        console.log("Login Page Mounted. Clearing Storage...");
        localStorage.clear(); // Nuclear option: Clear everything
        sessionStorage.clear();
    }, []);

    const onSubmit = async (data) => {
        setIsLoading(true);
        console.log("Attempting Login...");

        try {
            // 2. Perform Login
            const success = await login(data.username, data.password);

            if (success) {
                const token = localStorage.getItem('authToken');
                console.log("Login Successful. New Token:", token ? token.substring(0, 20) + "..." : "NULL");

                if (!token) {
                    toast.error("Login failed: Backend did not return a token.");
                    setIsLoading(false);
                    return;
                }

                toast.success('Access Granted');

                // 3. Force Browser to Reload completely to clear memory
                // Adding a timestamp ensures the browser treats it as a new navigation
                window.location.href = `/?refresh=${Date.now()}`;
            } else {
                toast.error('Invalid Credentials');
                setIsLoading(false);
            }
        } catch (e) {
            console.error("Login Error:", e);
            toast.error("Login Error");
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex bg-trade-bg font-sans items-center justify-center">
            <div className="w-full max-w-[420px] p-8">
                <h3 className="text-3xl font-bold text-white mb-6">AlgoTrade Pro</h3>
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                    <div>
                        <label className="block text-sm font-semibold text-slate-300 mb-2">Username</label>
                        <input {...register('username')} className="w-full p-4 bg-trade-panel border border-slate-700 rounded-xl text-white outline-none" placeholder="Username" />
                    </div>
                    <div>
                        <label className="block text-sm font-semibold text-slate-300 mb-2">Password</label>
                        <input type="password" {...register('password')} className="w-full p-4 bg-trade-panel border border-slate-700 rounded-xl text-white outline-none" placeholder="Password" />
                    </div>
                    <button disabled={isLoading} className="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-4 rounded-xl disabled:opacity-50">
                        {isLoading ? 'Verifying...' : 'Sign In'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Login;