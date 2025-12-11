import { create } from 'zustand';
import api from '../lib/api';

const useAuthStore = create((set, get) => ({
    user: null,
    isAuthenticated: !!localStorage.getItem('authToken'),
    token: localStorage.getItem('authToken'),

    login: async (username, password) => {
        try {
            console.log('🔐 Attempting login for:', username);

            const res = await api.post('/auth/login', { username, password });
            const { token } = res.data;

            if (!token) {
                console.error('❌ No token in response');
                return false;
            }

            console.log('✅ Token received:', token.substring(0, 20) + '...');

            // Save to localStorage FIRST
            localStorage.setItem('authToken', token);

            // Wait a bit to ensure localStorage write completes
            await new Promise(resolve => setTimeout(resolve, 100));

            // Verify it was saved
            const savedToken = localStorage.getItem('authToken');
            if (!savedToken) {
                console.error('❌ Token failed to save to localStorage');
                return false;
            }

            console.log('✅ Token persisted to localStorage');

            // Update Zustand state
            set({
                isAuthenticated: true,
                token: savedToken,
                user: { username }
            });

            console.log('✅ Zustand state updated');

            // Final verification
            const state = get();
            console.log('🔍 Final State Check:', {
                isAuthenticated: state.isAuthenticated,
                hasToken: !!state.token,
                hasUser: !!state.user
            });

            return true;
        } catch (error) {
            console.error('❌ Login failed:', error.response?.data || error.message);
            return false;
        }
    },

    logout: () => {
        console.log('👋 Logging out...');
        localStorage.removeItem('authToken');
        set({ isAuthenticated: false, token: null, user: null });
    },
}));

export default useAuthStore;