import { lingui } from "@lingui/vite-plugin"
import react from "@vitejs/plugin-react"
import { visualizer } from "rollup-plugin-visualizer"
import { defineConfig, PluginOption } from "vite"
import eslint from "vite-plugin-eslint"
import tsconfigPaths from "vite-tsconfig-paths"

// inject custom js and css links in html
const customCodeInjector: PluginOption = {
    name: "customCodeInjector",
    transformIndexHtml: html => {
        return {
            html,
            tags: [
                {
                    tag: "script",
                    attrs: {
                        src: "custom_js.js",
                    },
                    injectTo: "body",
                },
                {
                    tag: "link",
                    attrs: {
                        rel: "stylesheet",
                        href: "custom_css.css",
                    },
                    injectTo: "head",
                },
            ],
        }
    },
}

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        customCodeInjector,
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
            "/swagger": "http://localhost:8083",
            "/custom_css.css": "http://localhost:8083",
            "/custom_js.js": "http://localhost:8083",
        },
    },
    build: {
        chunkSizeWarningLimit: 3000,
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
