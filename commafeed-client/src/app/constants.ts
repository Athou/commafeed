import type { IconType } from "react-icons"
import { FaAt } from "react-icons/fa"
import { SiBuffer, SiFacebook, SiGmail, SiInstapaper, SiPocket, SiTumblr, SiX } from "react-icons/si"
import type { Category, Entry, SharingSettings } from "./types"

const categories: Record<string, Omit<Category, "name">> = {
    all: {
        id: "all",
        expanded: false,
        children: [],
        feeds: [],
        position: 0,
    },
    starred: {
        id: "starred",
        expanded: false,
        children: [],
        feeds: [],
        position: 1,
    },
}

const sharing: {
    [key in keyof SharingSettings]: {
        label: string
        icon: IconType
        color: `#${string}`
        url: (url: string, description: string) => string
    }
} = {
    email: {
        label: "Email",
        icon: FaAt,
        color: "#000000",
        url: (url, desc) => `mailto:?subject=${desc}&body=${url}`,
    },
    gmail: {
        label: "Gmail",
        icon: SiGmail,
        color: "#EA4335",
        url: (url, desc) => `https://mail.google.com/mail/?view=cm&fs=1&tf=1&source=mailto&su=${desc}&body=${url}`,
    },
    facebook: {
        label: "Facebook",
        icon: SiFacebook,
        color: "#1B74E4",
        url: url => `https://www.facebook.com/sharer/sharer.php?u=${url}`,
    },
    twitter: {
        label: "X",
        icon: SiX,
        color: "#000000",
        url: (url, desc) => `https://x.com/share?text=${desc}&url=${url}`,
    },
    tumblr: {
        label: "Tumblr",
        icon: SiTumblr,
        color: "#375672",
        url: (url, desc) => `https://www.tumblr.com/share/link?url=${url}&name=${desc}`,
    },
    pocket: {
        label: "Pocket",
        icon: SiPocket,
        color: "#EF4154",
        url: (url, desc) => `https://getpocket.com/save?url=${url}&title=${desc}`,
    },
    instapaper: {
        label: "Instapaper",
        icon: SiInstapaper,
        color: "#010101",
        url: (url, desc) => `https://www.instapaper.com/hello2?url=${url}&title=${desc}`,
    },
    buffer: {
        label: "Buffer",
        icon: SiBuffer,
        color: "#000000",
        url: (url, desc) => `https://bufferapp.com/add?url=${url}&text=${desc}`,
    },
}

export const Constants = {
    categories,
    sharing,
    layout: {
        mobileBreakpoint: 992,
        mobileBreakpointName: "md",
        headerHeight: 60,
        entryMaxWidth: 650,
        isTopVisible: (div: HTMLElement) => {
            const header = document.getElementById(Constants.dom.headerId)?.getBoundingClientRect()
            return div.getBoundingClientRect().top >= (header?.bottom ?? 0)
        },
        isBottomVisible: (div: HTMLElement) => {
            const footer = document.getElementById(Constants.dom.footerId)?.getBoundingClientRect()
            return div.getBoundingClientRect().bottom <= (footer?.top ?? window.innerHeight)
        },
    },
    dom: {
        headerId: "header",
        footerId: "footer",
        entryId: (entry: Entry) => `entry-id-${entry.id}`,
        entryContextMenuId: (entry: Entry) => entry.id,
    },
    theme: {
        defaultPrimaryColor: "orange",
    },
    tooltip: {
        delay: 500,
    },
    browserExtensionUrl: "https://github.com/Athou/commafeed-browser-extension",
    customCssDocumentationUrl: "https://athou.github.io/commafeed/documentation/custom-css",
    bitcoinWalletAddress: "1dymfUxqCWpyD7a6rQSqNy4rLVDBsAr5e",
}
