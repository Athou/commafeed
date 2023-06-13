import { useEffect, useState } from "react"

export const useBrowserExtension = () => {
    // the extension will set the "browser-extension-installed" attribute on the root element
    const [browserExtensionVersion, setBrowserExtensionVersion] = useState(
        document.documentElement.getAttribute("browser-extension-installed")
    )

    // monitor the attribute on the root element as it may change after the page was loaded
    useEffect(() => {
        const observer = new MutationObserver(mutations => {
            mutations.forEach(mutation => {
                if (mutation.type === "attributes") {
                    const element = mutation.target as Element
                    const version = element.getAttribute("browser-extension-installed")
                    if (version) setBrowserExtensionVersion(version)
                }
            })
        })

        observer.observe(document.documentElement, {
            attributes: true,
        })

        return () => observer.disconnect()
    }, [])

    // when not in an iframe, window.parent is a reference to window
    const isBrowserExtensionPopup = window.parent !== window
    const isBrowserExtensionInstalled = isBrowserExtensionPopup || !!browserExtensionVersion
    const isBrowserExtensionInstallable = !isBrowserExtensionPopup

    const w = isBrowserExtensionPopup ? window.parent : window
    const openSettingsPage = () => w.postMessage("open-settings-page", "*")
    const openAppInNewTab = () => w.postMessage("open-app-in-new-tab", "*")
    const openLinkInBackgroundTab = (url: string) => {
        if (isBrowserExtensionInstalled) {
            w.postMessage(`open-link-in-background-tab:${url}`, "*")
        } else {
            // fallback to ctrl+click simulation
            const a = document.createElement("a")
            a.href = url
            a.rel = "noreferrer"
            a.dispatchEvent(
                new MouseEvent("click", {
                    ctrlKey: true,
                    metaKey: true,
                })
            )
        }
    }
    const setBadgeUnreadCount = (count: number) => w.postMessage(`set-badge-unread-count:${count}`, "*")

    return {
        browserExtensionVersion,
        isBrowserExtensionInstallable,
        isBrowserExtensionInstalled,
        isBrowserExtensionPopup,
        openSettingsPage,
        openAppInNewTab,
        openLinkInBackgroundTab,
        setBadgeUnreadCount,
    }
}
