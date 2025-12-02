/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                // Trading specific colors
                trade: {
                    bg: '#0f172a',       // Deep blue-black background
                    panel: '#1e293b',    // Lighter panel color
                    border: '#334155',   // Border color
                    primary: '#3b82f6',  // Action button blue
                    buy: '#10b981',      // Green
                    sell: '#ef4444',     // Red
                    text: '#f8fafc',     // Main text
                    muted: '#94a3b8'     // Secondary text
                }
            }
        },
    },
    plugins: [],
}