import { i18n } from "@lingui/core"
import { useAppSelector } from "app/store"
import dayjs from "dayjs"
import { useEffect } from "react"

interface Locale {
    key: string
    label: string
    daysjsImportFn: () => Promise<ILocale>
}

// add an object to the array to add a new locale
// don't forget to also add it to the 'locales' array in .linguirc
export const locales: Locale[] = [
    { key: "ar", label: "العربية", daysjsImportFn: () => import("dayjs/locale/ar") },
    { key: "ca", label: "Català", daysjsImportFn: () => import("dayjs/locale/ca") },
    { key: "cs", label: "Čeština", daysjsImportFn: () => import("dayjs/locale/cs") },
    { key: "cy", label: "Cymraeg", daysjsImportFn: () => import("dayjs/locale/cy") },
    { key: "da", label: "Danish", daysjsImportFn: () => import("dayjs/locale/da") },
    { key: "de", label: "Deutsch", daysjsImportFn: () => import("dayjs/locale/de") },
    { key: "en", label: "English", daysjsImportFn: () => import("dayjs/locale/en") },
    { key: "es", label: "Español", daysjsImportFn: () => import("dayjs/locale/es") },
    { key: "fa", label: "فارسی", daysjsImportFn: () => import("dayjs/locale/fa") },
    { key: "fi", label: "Suomi", daysjsImportFn: () => import("dayjs/locale/fi") },
    { key: "fr", label: "Français", daysjsImportFn: () => import("dayjs/locale/fr") },
    { key: "gl", label: "Galician", daysjsImportFn: () => import("dayjs/locale/gl") },
    { key: "hu", label: "Magyar", daysjsImportFn: () => import("dayjs/locale/hu") },
    { key: "id", label: "Indonesian", daysjsImportFn: () => import("dayjs/locale/id") },
    { key: "it", label: "Italiano", daysjsImportFn: () => import("dayjs/locale/it") },
    { key: "ja", label: "日本語", daysjsImportFn: () => import("dayjs/locale/ja") },
    { key: "ko", label: "한국어", daysjsImportFn: () => import("dayjs/locale/ko") },
    { key: "ms", label: "Bahasa Malaysian", daysjsImportFn: () => import("dayjs/locale/ms") },
    { key: "nb", label: "Norsk (bokmål)", daysjsImportFn: () => import("dayjs/locale/nb") },
    { key: "nl", label: "Nederlands", daysjsImportFn: () => import("dayjs/locale/nl") },
    { key: "nn", label: "Norsk (nynorsk)", daysjsImportFn: () => import("dayjs/locale/nn") },
    { key: "pl", label: "Polski", daysjsImportFn: () => import("dayjs/locale/pl") },
    { key: "pt", label: "Português", daysjsImportFn: () => import("dayjs/locale/pt") },
    { key: "ru", label: "Русский", daysjsImportFn: () => import("dayjs/locale/ru") },
    { key: "sk", label: "Slovenčina", daysjsImportFn: () => import("dayjs/locale/sk") },
    { key: "sv", label: "Svenska", daysjsImportFn: () => import("dayjs/locale/sv") },
    { key: "tr", label: "Türkçe", daysjsImportFn: () => import("dayjs/locale/tr") },
    { key: "zh", label: "简体中文", daysjsImportFn: () => import("dayjs/locale/zh") },
]

function activateLocale(locale: string) {
    // lingui
    import(`./locales/${locale}/messages.po`).then(data => {
        i18n.load(locale, data.messages)
        i18n.activate(locale)
    })

    // dayjs
    locales
        .find(l => l.key === locale)
        ?.daysjsImportFn()
        .then(() => dayjs.locale(locale))
}

export const useI18n = () => {
    const locale = useAppSelector(state => state.user.settings?.language)
    useEffect(() => {
        activateLocale(locale ?? "en")
    }, [locale])
}
