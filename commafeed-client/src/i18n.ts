import { i18n, Messages } from "@lingui/core"
import { useAppSelector } from "app/store"
import dayjs from "dayjs"
import "dayjs/locale/en"
import "dayjs/locale/fr"
import { en, fr } from "make-plural"
import { useEffect } from "react"
import { messages as enMessages } from "./locales/en/messages"
import { messages as frMessages } from "./locales/fr/messages"

interface Locale {
    key: string
    label: string
    messages: Messages
    plurals?: (n: number | string, ord?: boolean) => string
}

// add an object to the array to add a new locale
// don't forget to also add it to the 'locales' array in .linguirc
export const locales: Locale[] = [
    {
        key: "en",
        label: "English",
        messages: enMessages,
        plurals: en,
    },
    {
        key: "fr",
        label: "Français",
        messages: frMessages,
        plurals: fr,
    },
]

locales.forEach(l => {
    i18n.loadLocaleData({
        [l.key]: {
            plurals: l.plurals,
        },
    })
    i18n.load({
        [l.key]: l.messages,
    })
})

function activateLocale(locale: string) {
    i18n.activate(locale)
    dayjs.locale(locale)
}

export const useI18n = () => {
    const locale = useAppSelector(state => state.user.settings?.language)
    useEffect(() => {
        activateLocale(locale ?? "en")
    }, [locale])
}
