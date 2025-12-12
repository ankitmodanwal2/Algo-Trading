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

    // Don't attach token for auth endpoints
    if (config.url.includes('/auth/')) return config;

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    } else {
        console.warn(`[API] BLOCKED request to ${config.url} (No Token)`);
        const controller = new AbortController();
        config.signal = controller.signal;
        controller.abort("No Auth Token");
        return Promise.reject(new axios.Cancel("No Auth Token"));
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (axios.isCancel(error)) return new Promise(() => {});

        const status = error.response?.status;
        // Check if the request config had the 'silent' flag
        const isSilent = error.config?.silent;

        if (status === 401) {
            console.error(`[API] 401 Unauthorized for ${error.config?.url}`);

            // 🌟 FIX: Only show Toast if NOT silent.
            // This stops the popup spam for background polling.
            if (!isSilent) {
                toast.error("Session Sync Error. Please refresh manually.");
            }
        }
        return Promise.reject(error);
    }
);

export default api;