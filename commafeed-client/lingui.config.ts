import type { LinguiConfig } from "@lingui/conf"

const config: LinguiConfig = {
    locales: [
        "ar",
        "ca",
        "cs",
        "cy",
        "da",
        "de",
        "en",
        "es",
        "fa",
        "fi",
        "fr",
        "gl",
        "hu",
        "id",
        "it",
        "ja",
        "ko",
        "ms",
        "nb",
        "nl",
        "nn",
        "pl",
        "pt",
        "ru",
        "sk",
        "sv",
        "tr",
        "zh",
    ],
    catalogs: [
        {
            path: "src/locales/{locale}/messages",
            include: ["src"],
            exclude: ["src/locales/**"],
        },
    ],
    format: "po",
    formatOptions: {
        origins: true,
        lineNumbers: false,
    },
    sourceLocale: "en",
    fallbackLocales: {
        default: "en",
    },
}

export default config
