import { i18n, Messages } from "@lingui/core"
import { useAppSelector } from "app/store"
import dayjs from "dayjs"
import "dayjs/locale/ar"
import "dayjs/locale/ca"
import "dayjs/locale/cs"
import "dayjs/locale/cy"
import "dayjs/locale/da"
import "dayjs/locale/de"
import "dayjs/locale/en"
import "dayjs/locale/es"
import "dayjs/locale/fa"
import "dayjs/locale/fi"
import "dayjs/locale/fr"
import "dayjs/locale/gl"
import "dayjs/locale/hu"
import "dayjs/locale/id"
import "dayjs/locale/it"
import "dayjs/locale/ja"
import "dayjs/locale/ko"
import "dayjs/locale/ms"
import "dayjs/locale/nb"
import "dayjs/locale/nl"
import "dayjs/locale/nn"
import "dayjs/locale/pl"
import "dayjs/locale/pt"
import "dayjs/locale/ru"
import "dayjs/locale/sk"
import "dayjs/locale/sv"
import "dayjs/locale/tr"
import "dayjs/locale/zh"

import { useEffect } from "react"
import { messages as arMessages } from "./locales/ar/messages"
import { messages as caMessages } from "./locales/ca/messages"
import { messages as csMessages } from "./locales/cs/messages"
import { messages as cyMessages } from "./locales/cy/messages"
import { messages as daMessages } from "./locales/da/messages"
import { messages as deMessages } from "./locales/de/messages"
import { messages as enMessages } from "./locales/en/messages"
import { messages as esMessages } from "./locales/es/messages"
import { messages as faMessages } from "./locales/fa/messages"
import { messages as fiMessages } from "./locales/fi/messages"
import { messages as frMessages } from "./locales/fr/messages"
import { messages as glMessages } from "./locales/gl/messages"
import { messages as huMessages } from "./locales/hu/messages"
import { messages as idMessages } from "./locales/id/messages"
import { messages as itMessages } from "./locales/it/messages"
import { messages as jaMessages } from "./locales/ja/messages"
import { messages as koMessages } from "./locales/ko/messages"
import { messages as msMessages } from "./locales/ms/messages"
import { messages as nbMessages } from "./locales/nb/messages"
import { messages as nlMessages } from "./locales/nl/messages"
import { messages as nnMessages } from "./locales/nn/messages"
import { messages as plMessages } from "./locales/pl/messages"
import { messages as ptMessages } from "./locales/pt/messages"
import { messages as ruMessages } from "./locales/ru/messages"
import { messages as skMessages } from "./locales/sk/messages"
import { messages as svMessages } from "./locales/sv/messages"
import { messages as trMessages } from "./locales/tr/messages"
import { messages as zhMessages } from "./locales/zh/messages"

interface Locale {
    key: string
    label: string
    messages: Messages
}

// add an object to the array to add a new locale
// don't forget to also add it to the 'locales' array in .linguirc
export const locales: Locale[] = [
    { key: "ar", messages: arMessages, label: "العربية" },
    { key: "ca", messages: caMessages, label: "Català" },
    { key: "cs", messages: csMessages, label: "Čeština" },
    { key: "cy", messages: cyMessages, label: "Cymraeg" },
    { key: "da", messages: daMessages, label: "Danish" },
    { key: "de", messages: deMessages, label: "Deutsch" },
    { key: "en", messages: enMessages, label: "English" },
    { key: "es", messages: esMessages, label: "Español" },
    { key: "fa", messages: faMessages, label: "فارسی" },
    { key: "fi", messages: fiMessages, label: "Suomi" },
    { key: "fr", messages: frMessages, label: "Français" },
    { key: "gl", messages: glMessages, label: "Galician" },
    { key: "hu", messages: huMessages, label: "Magyar" },
    { key: "id", messages: idMessages, label: "Indonesian" },
    { key: "it", messages: itMessages, label: "Italiano" },
    { key: "ja", messages: jaMessages, label: "日本語" },
    { key: "ko", messages: koMessages, label: "한국어" },
    { key: "ms", messages: msMessages, label: "Bahasa Malaysian" },
    { key: "nb", messages: nbMessages, label: "Norsk (bokmål)" },
    { key: "nl", messages: nlMessages, label: "Nederlands" },
    { key: "nn", messages: nnMessages, label: "Norsk (nynorsk)" },
    { key: "pl", messages: plMessages, label: "Polski" },
    { key: "pt", messages: ptMessages, label: "Português" },
    { key: "ru", messages: ruMessages, label: "Русский" },
    { key: "sk", messages: skMessages, label: "Slovenčina" },
    { key: "sv", messages: svMessages, label: "Svenska" },
    { key: "tr", messages: trMessages, label: "Türkçe" },
    { key: "zh", messages: zhMessages, label: "简体中文" },
]

locales.forEach(l => {
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
