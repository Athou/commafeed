import { lingui } from "@lingui/vite-plugin"
import babel from "@rolldown/plugin-babel"
import react, { reactCompilerPreset } from "@vitejs/plugin-react"
import { defineConfig } from "vite"
import checker from "vite-plugin-checker"

export default defineConfig(() => ({
    plugins: [
        react(),
        babel({
            presets: [reactCompilerPreset()],
            plugins: ["@lingui/babel-plugin-lingui-macro"],
        }),
        lingui(),
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
            "/openapi": "http://localhost:8083",
            "/api-documentation": "http://localhost:8083",
            "/custom_css.css": "http://localhost:8083",
            "/custom_js.js": "http://localhost:8083",
            "/j_security_check": "http://localhost:8083",
            "/logout": "http://localhost:8083",
        },
    },
    resolve: {
        tsconfigPaths: true,
    },
    legacy: {
        // required for websocket-heartbeat-js
        inconsistentCjsInterop: true,
    },
    test: {
        environment: "jsdom",
        globals: true,
        setupFiles: "./src/setupTests.ts",
    },
    build: {
        chunkSizeWarningLimit: 4000,
        rolldownOptions: {
            output: {
                codeSplitting: {
                    groups: [
                        // output mantine as its own chunk because it is quite large
                        {
                            name: "mantine",
                            test: "@mantine",
                        },
                    ],
                },
            },
        },
    },
}))
