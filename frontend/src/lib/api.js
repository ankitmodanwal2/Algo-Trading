import axios from 'axios';
import toast from 'react-hot-toast';

const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

// ===== REQUEST INTERCEPTOR =====
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('authToken');

    // Check public endpoints
    const publicEndpoints = ['/auth/login', '/auth/register'];
    const isPublic = publicEndpoints.some(endpoint => config.url.includes(endpoint));

    if (isPublic) {
        return config;
    }

    if (token) {
        // ✅ CRITICAL FIX: Use .set() for Axios 1.6+ compatibility
        config.headers.set('Authorization', `Bearer ${token}`);

        // 🔍 DEBUG LOG: Print to console to prove we attached it
        console.log(`🔑 Attached Token to: ${config.url}`);
    } else {
        // Block request if no token
        console.warn(`⛔ Blocking request to ${config.url} (No Token)`);
        const controller = new AbortController();
        config.signal = controller.signal;
        controller.abort("No Auth Token Found");
    }

    return config;
}, (error) => {
    return Promise.reject(error);
});

// ===== RESPONSE INTERCEPTOR =====
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Ignore our own blocks
        if (axios.isCancel(error) || error.message === "No Auth Token Found") {
            return Promise.reject(error);
        }

        const status = error.response?.status;
        const currentPath = window.location.pathname;

        if (status === 401) {
            const hasToken = localStorage.getItem('authToken');
            if (hasToken && !currentPath.includes('/login')) {
                // We just warn for now to keep your dashboard open for debugging
                console.warn('⚠️ Server returned 401 Unauthorized.');
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