import axios from 'axios';
import toast from 'react-hot-toast';

const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('authToken');
    const publicEndpoints = ['/auth/login', '/auth/register'];
    const isPublic = publicEndpoints.some(endpoint => config.url.includes(endpoint));

    if (!isPublic) {
        if (token) {
            config.headers.set('Authorization', `Bearer ${token}`);
        } else {
            const controller = new AbortController();
            config.signal = controller.signal;
            controller.abort("No Auth Token Found");
        }
    }
    return config;
}, (error) => Promise.reject(error));

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (axios.isCancel(error) || error.message === "No Auth Token Found") {
            return Promise.reject(error);
        }

        const status = error.response?.status;
        const currentPath = window.location.pathname;
        const url = error.config?.url || "";

        if (status === 401) {
            const hasToken = localStorage.getItem('authToken');

            // 🛡️ SMART FILTER: Don't show toast/logout for background polling failures
            const isBackgroundPoll = url.includes('/positions') || url.includes('/marketdata');

            if (hasToken && !currentPath.includes('/login') && !isBackgroundPoll) {
                console.warn('⚠️ Session expired. Logging out...');
                localStorage.removeItem('authToken');
                window.location.href = '/login';
            }
        }
        else if (status >= 500) {
            const msg = error.response?.data?.message || '';
            if(!msg.includes("Dhan Error")) {
                toast.error('Server error. Please try again.');
            }
        }

        return Promise.reject(error);
    }
);

export default api;