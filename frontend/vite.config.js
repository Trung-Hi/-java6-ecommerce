import {defineConfig} from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "src")
        }
    },
    server: {
        proxy: {
            "/api": {
                target: process.env.VITE_BACKEND_URL || "http://localhost:8080",
                changeOrigin: true
            },
            "/images": {
                target: process.env.VITE_BACKEND_URL || "http://localhost:8080",
                changeOrigin: true
            },
            "/css": {
                target: process.env.VITE_BACKEND_URL || "http://localhost:8080",
                changeOrigin: true
            },
            "/oauth2": {
                target: process.env.VITE_BACKEND_URL || "http://localhost:8080",
                changeOrigin: true
            },
            "/login": {
                target: process.env.VITE_BACKEND_URL || "http://localhost:8080",
                changeOrigin: true
            },
            "/ws": {
                target: process.env.VITE_BACKEND_URL || "http://localhost:8080",
                changeOrigin: true,
                ws: true
            }
        }
    },
    build: {
        outDir: "../src/main/resources/static/app",
        emptyOutDir: true
    }
});
