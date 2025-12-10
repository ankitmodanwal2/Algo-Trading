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
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    } else {
        console.warn(`[API] Warning: Sending request to ${config.url} WITHOUT token.`);
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;

        if (status === 401) {
            console.error("Authentication Failed (401).");
            // toast.error("Session expired. Please manually logout.");

            // --- CRITICAL FIX: COMMENT OUT THESE LINES TO STOP THE LOOP ---
            // localStorage.removeItem('authToken');
            // window.location.href = '/login';
            // ---------------------------------------------------------------
        }
        return Promise.reject(error);
    }
);

export default api;