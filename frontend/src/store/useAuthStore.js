import { create } from 'zustand';
import api from '../lib/api';

const useAuthStore = create((set) => ({
    user: null,
    isAuthenticated: !!localStorage.getItem('authToken'),
    token: localStorage.getItem('authToken'),

    login: async (username, password) => {
        try {
            console.log("Sending Login Request...");
            const res = await api.post('/auth/login', { username, password });

            // Extract token
            const { token } = res.data;
            console.log("Login Response Received. Token length:", token ? token.length : 0);

            if (token) {
                localStorage.setItem('authToken', token);
                set({ isAuthenticated: true, token, user: { username } });
                return true;
            }
            return false;
        } catch (error) {
            console.error('Login failed', error);
            return false;
        }
    },

    logout: () => {
        localStorage.removeItem('authToken');
        set({ isAuthenticated: false, token: null, user: null });
    },
}));

export default useAuthStore;