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

            localStorage.setItem('authToken', token);
            set({ isAuthenticated: true, token, user: { username } });
            return true;
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