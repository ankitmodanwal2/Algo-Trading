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

    // Public endpoints that don't need authentication
    const publicEndpoints = ['/auth/login', '/auth/register'];
    const isPublic = publicEndpoints.some(endpoint => config.url.includes(endpoint));

    // ✅ FIX: Only add token if it exists (don't block requests without token)
    if (token && !isPublic) {
        config.headers.Authorization = `Bearer ${token}`;
        console.log('🔑 Adding token to request:', config.url);
    } else if (!token && !isPublic) {
        console.warn('⚠️ No token found for protected endpoint:', config.url);
    }

    return config;
}, (error) => {
    return Promise.reject(error);
});

// ===== RESPONSE INTERCEPTOR =====
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Ignore cancelled requests
        if (axios.isCancel(error)) {
            return new Promise(() => {});
        }

        const status = error.response?.status;
        const currentPath = window.location.pathname;

        // ✅ FIX: Only handle 401 if we're NOT on login page AND we have a token
        if (status === 401) {
            const hasToken = localStorage.getItem('authToken');

            if (hasToken && !currentPath.includes('/login')) {
                console.error('❌ 401 Unauthorized - Session expired');
                localStorage.removeItem('authToken');
                toast.error('Session expired. Please login again.');

                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
            }
        } else if (status >= 500) {
            toast.error('Server error. Please try again.');
        } else if (status === 403) {
            toast.error('Access denied');
        } else if (status === 400) {
            // Don't show toast for 400 errors, let component handle them
            console.warn('Bad request:', error.response?.data);
        }

        return Promise.reject(error);
    }
);

export default api;