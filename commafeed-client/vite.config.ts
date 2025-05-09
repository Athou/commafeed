import { lingui } from "@lingui/vite-plugin"
import react from "@vitejs/plugin-react"
import { visualizer } from "rollup-plugin-visualizer"
import { defineConfig } from "vite"
import checker from "vite-plugin-checker"
import tsconfigPaths from "vite-tsconfig-paths"

// https://vitejs.dev/config/
export default defineConfig(() => ({
    plugins: [
        react({
            babel: {
                plugins: ["@lingui/babel-plugin-lingui-macro"],
            },
        }),
        lingui(),
        tsconfigPaths(),
        visualizer(),
        checker({
            typescript: true,
            biome: {
                command: "check",
            },
        }),
    ],
    base: "./",
    server: {
        port: 8082,
        proxy: {
            "/rest": "http://127.0.0.1:8083",
            "/next": "http://127.0.0.1:8083",
            "/ws": "ws://127.0.0.1:8083",
            "/openapi.json": "http://127.0.0.1:8083",
            "/custom_css.css": "http://127.0.0.1:8083",
            "/custom_js.js": "http://127.0.0.1:8083",
            "/j_security_check": "http://127.0.0.1:8083",
            "/logout": "http://127.0.0.1:8083",
        },
    },
    build: {
        chunkSizeWarningLimit: 3500,
        rollupOptions: {
            output: {
                manualChunks: id => {
                    if (id.includes("@mantine")) {
                        return "mantine"
                    }
                },
            },
        },
    },
    test: {
        environment: "jsdom",
        globals: true,
        setupFiles: "./src/setupTests.ts",
    },
}))
