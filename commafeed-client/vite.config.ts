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
        eslint(),
        tsconfigPaths(),
        visualizer(),
    ],
    base: "./",
    server: {
        port: 8082,
        proxy: {
            "/rest": "http://localhost:8083",
        },
    },
    build: {
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
