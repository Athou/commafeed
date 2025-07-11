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
                plugins: [
                    // support for lingui macros
                    // needs to be before the react compiler plugin
                    "@lingui/babel-plugin-lingui-macro",
                    // react compiler
                    ["babel-plugin-react-compiler", { target: "19" }],
                ],
            },
        }),
        lingui(),
        tsconfigPaths(),
        visualizer(),
        checker({
            typescript: true,
            biome: {
                command: "check",
                flags: "--error-on-warnings",
            },
        }),
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
            "/j_security_check": "http://localhost:8083",
            "/logout": "http://localhost:8083",
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
    test: {
        environment: "jsdom",
        globals: true,
        setupFiles: "./src/setupTests.ts",
    },
}))
