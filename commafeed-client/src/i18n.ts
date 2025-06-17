import { i18n, type Messages } from "@lingui/core"
import { useAppSelector } from "app/store"
import dayjs from "dayjs"
import { useEffect } from "react"

interface Locale {
    key: string
    label: string
    dayjsImportFn: () => Promise<ILocale>
}

// add an object to the array to add a new locale
// don't forget to also add it to the 'locales' array in lingui.config.ts
export const locales: Locale[] = [
    { key: "ar", label: "العربية", dayjsImportFn: async () => await import("dayjs/locale/ar") },
    { key: "ca", label: "Català", dayjsImportFn: async () => await import("dayjs/locale/ca") },
    { key: "cs", label: "Čeština", dayjsImportFn: async () => await import("dayjs/locale/cs") },
    { key: "cy", label: "Cymraeg", dayjsImportFn: async () => await import("dayjs/locale/cy") },
    { key: "da", label: "Danish", dayjsImportFn: async () => await import("dayjs/locale/da") },
    { key: "de", label: "Deutsch", dayjsImportFn: async () => await import("dayjs/locale/de") },
    { key: "en", label: "English", dayjsImportFn: async () => await import("dayjs/locale/en") },
    { key: "es", label: "Español", dayjsImportFn: async () => await import("dayjs/locale/es") },
    { key: "fa", label: "فارسی", dayjsImportFn: async () => await import("dayjs/locale/fa") },
    { key: "fi", label: "Suomi", dayjsImportFn: async () => await import("dayjs/locale/fi") },
    { key: "fr", label: "Français", dayjsImportFn: async () => await import("dayjs/locale/fr") },
    { key: "gl", label: "Galician", dayjsImportFn: async () => await import("dayjs/locale/gl") },
    { key: "hu", label: "Magyar", dayjsImportFn: async () => await import("dayjs/locale/hu") },
    { key: "id", label: "Indonesian", dayjsImportFn: async () => await import("dayjs/locale/id") },
    { key: "it", label: "Italiano", dayjsImportFn: async () => await import("dayjs/locale/it") },
    { key: "ja", label: "日本語", dayjsImportFn: async () => await import("dayjs/locale/ja") },
    { key: "ko", label: "한국어", dayjsImportFn: async () => await import("dayjs/locale/ko") },
    { key: "ms", label: "Bahasa Malaysian", dayjsImportFn: async () => await import("dayjs/locale/ms") },
    { key: "nb", label: "Norsk (bokmål)", dayjsImportFn: async () => await import("dayjs/locale/nb") },
    { key: "nl", label: "Nederlands", dayjsImportFn: async () => await import("dayjs/locale/nl") },
    { key: "nn", label: "Norsk (nynorsk)", dayjsImportFn: async () => await import("dayjs/locale/nn") },
    { key: "pl", label: "Polski", dayjsImportFn: async () => await import("dayjs/locale/pl") },
    { key: "pt", label: "Português", dayjsImportFn: async () => await import("dayjs/locale/pt") },
    { key: "ru", label: "Русский", dayjsImportFn: async () => await import("dayjs/locale/ru") },
    { key: "sk", label: "Slovenčina", dayjsImportFn: async () => await import("dayjs/locale/sk") },
    { key: "sv", label: "Svenska", dayjsImportFn: async () => await import("dayjs/locale/sv") },
    { key: "tr", label: "Türkçe", dayjsImportFn: async () => await import("dayjs/locale/tr") },
    { key: "zh", label: "简体中文", dayjsImportFn: async () => await import("dayjs/locale/zh") },
]

function activateLocale(locale: string) {
    // lingui
    import(`./locales/${locale}/messages.po`).then((data: { messages: Messages }) => {
        i18n.load(locale, data.messages)
        i18n.activate(locale)
    })

    // dayjs
    locales
        .find(l => l.key === locale)
        ?.dayjsImportFn()
        .then(() => dayjs.locale(locale))
}

export const useI18n = () => {
    const locale =
        useAppSelector(state => state.user.settings?.language) ??
        navigator.languages.map(l => l.split("-")[0]).find(l => locales.some(locale => locale.key === l)) ??
        "en"

    useEffect(() => {
        activateLocale(locale)
    }, [locale])
}
