import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import DashboardLayout from './layout/DashboardLayout';
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import Brokers from './pages/Brokers';       // <--- Import
import Strategies from './pages/Strategies'; // <--- Import
import Settings from './pages/Settings';     // <--- Import
import useAuthStore from './store/useAuthStore';

// Protected Route Wrapper
const ProtectedRoute = ({ children }) => {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    return isAuthenticated ? children : <Navigate to="/login" />;
};

function App() {
    return (
        <BrowserRouter>
            <Toaster position="top-right" toastOptions={{ style: { background: '#1e293b', color: '#fff' } }} />
            <Routes>
                <Route path="/login" element={<Login />} />

                <Route path="/" element={
                    <ProtectedRoute>
                        <DashboardLayout />
                    </ProtectedRoute>
                }>
                    <Route index element={<Dashboard />} />
                    <Route path="brokers" element={<Brokers />} />       {/* <--- Route */}
                    <Route path="strategies" element={<Strategies />} /> {/* <--- Route */}
                    <Route path="settings" element={<Settings />} />     {/* <--- Route */}
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;