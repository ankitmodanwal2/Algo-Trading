import { create } from 'zustand';
import api from '../lib/api';

const useAuthStore = create((set) => ({
    user: null,
    isAuthenticated: !!localStorage.getItem('authToken'),
    token: localStorage.getItem('authToken'),

    login: async (username, password) => {
        try {
            const res = await api.post('/auth/login', { username, password });
            const { token } = res.data;

            if (token) {
                // ✅ FIX: Save token SYNCHRONOUSLY before updating state
                localStorage.setItem('authToken', token);

                // ✅ FIX: Force synchronous write (some browsers need this)
                await new Promise(resolve => setTimeout(resolve, 50));

                // ✅ FIX: Verify token was saved
                const savedToken = localStorage.getItem('authToken');
                if (!savedToken) {
                    console.error('❌ Token failed to save to localStorage!');
                    return false;
                }

                console.log('✅ Token saved successfully:', savedToken.substring(0, 20) + '...');

                // Now update Zustand state
                set({
                    isAuthenticated: true,
                    token: savedToken,
                    user: { username }
                });

                return true;
            }
            return false;
        } catch (error) {
            console.error('❌ Login failed:', error);
            return false;
        }
    },

    logout: () => {
        localStorage.removeItem('authToken');
        set({ isAuthenticated: false, token: null, user: null });
    },
}));

export default useAuthStore;