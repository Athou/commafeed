import { lingui } from "@lingui/vite-plugin"
import react from "@vitejs/plugin-react"
import { visualizer } from "rollup-plugin-visualizer"
import { defineConfig } from "vite"
import eslint from "vite-plugin-eslint"
import tsconfigPaths from "vite-tsconfig-paths"

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        react({
            babel: {
                // babel-macro is needed for lingui
                plugins: ["macros"],
            },
        }),
        lingui(),
        eslint(),
        tsconfigPaths(),
        visualizer(),
    ],
    base: "./",
    server: {
        port: 8082,
        proxy: {
            "/rest": "http://localhost:8083",
            "/next": "http://localhost:8083",
            "/ws": "ws://localhost:8083",
            "/openapi.json": "http://localhost:8083",
            "/custom_css.css": "http://localhost:8083",
            "/custom_js.js": "http://localhost:8083",
        },
    },
    build: {
        chunkSizeWarningLimit: 3500,
        rollupOptions: {
            output: {
                manualChunks: id => {
                    // output mantine as its own chunk because it is quite large
                    if (id.includes("@mantine")) {
                        return "mantine"
                    }
                },
            },
        },
    },
})
