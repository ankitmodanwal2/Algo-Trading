import React from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { LayoutDashboard, LineChart, Wallet, LogOut, Settings } from 'lucide-react';
import useAuthStore from '../store/useAuthStore';

const NavItem = ({ to, icon: Icon, label }) => {
    const location = useLocation();
    const isActive = location.pathname === to;

    return (
        <Link
            to={to}
            className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                isActive
                    ? 'bg-trade-primary text-white'
                    : 'text-trade-muted hover:bg-trade-panel hover:text-trade-text'
            }`}
        >
            <Icon size={20} />
            <span className="font-medium">{label}</span>
        </Link>
    );
};

const DashboardLayout = () => {
    const logout = useAuthStore((state) => state.logout);
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="flex h-screen bg-trade-bg text-trade-text overflow-hidden">
            {/* Sidebar */}
            <aside className="w-64 bg-trade-panel border-r border-trade-border flex flex-col">
                <div className="p-6 border-b border-trade-border">
                    <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-cyan-400 bg-clip-text text-transparent">
                        AlgoTrade
                    </h1>
                </div>

                <nav className="flex-1 p-4 space-y-2">
                    <NavItem to="/" icon={LayoutDashboard} label="Dashboard" />
                    <NavItem to="/strategies" icon={LineChart} label="Strategies" />
                    <NavItem to="/brokers" icon={Wallet} label="Brokers" />
                    <NavItem to="/settings" icon={Settings} label="Settings" />
                </nav>

                <div className="p-4 border-t border-trade-border">
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 px-4 py-3 w-full text-red-400 hover:bg-red-400/10 rounded-lg transition-colors"
                    >
                        <LogOut size={20} />
                        <span>Logout</span>
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 overflow-y-auto">
                <header className="h-16 bg-trade-bg/50 backdrop-blur border-b border-trade-border sticky top-0 z-10 px-8 flex items-center justify-between">
                    <h2 className="text-lg font-medium text-trade-muted">Overview</h2>
                    <div className="flex items-center gap-4">
            <span className="text-sm text-green-400 px-3 py-1 bg-green-400/10 rounded-full">
              ● System Online
            </span>
                    </div>
                </header>
                <div className="p-8">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};

export default DashboardLayout;