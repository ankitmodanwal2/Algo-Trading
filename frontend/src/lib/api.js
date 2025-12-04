import axios from 'axios';
import toast from 'react-hot-toast';

const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use((config) => {
    // Always fetch the latest token from storage right before the request flies
    const token = localStorage.getItem('authToken');

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        // Debug log to verify token attachment
        console.log(`[API] Attaching Token to ${config.url}`);
    } else {
        // Warning if request is flying without auth
        console.warn(`[API] No Token found for ${config.url}`);
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        console.error("API Error:", error);
        if (error.response?.status === 401) {
            // TOAST ONLY - DO NOT REDIRECT while debugging
            toast.error("Session Invalid (401). Check Console.");
        }
        return Promise.reject(error);
    }
);

export default api;