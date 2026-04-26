import type { LinguiConfig } from "@lingui/conf"
import { formatter } from "@lingui/format-po"

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
    format: formatter({
        lineNumbers: false,
    }),
    sourceLocale: "en",
    fallbackLocales: {
        default: "en",
    },
}

export default config
