import { useEffect, useState } from "react"

export const useBrowserExtension = () => {
    const [browserExtensionVersion, setBrowserExtensionVersion] = useState<string>()

    // the extension will set the "browser-extension-installed" attribute on the root element, monitor it for changes
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

    const isBrowserExtensionInstalled = !!browserExtensionVersion
    // when not in an iframe, window.parent is a reference to window
    const isBrowserExtensionPopup = window.parent !== window
    const isBrowserExtensionInstallable = !isBrowserExtensionPopup

    const w = isBrowserExtensionPopup ? window.parent : window
    const openSettingsPage = () => w.postMessage("open-settings-page", "*")
    const openAppInNewTab = () => w.postMessage("open-app-in-new-tab", "*")
    const openLinkInBackgroundTab = (url: string) => w.postMessage(`open-link-in-background-tab:${url}`, "*")

    return {
        browserExtensionVersion,
        isBrowserExtensionInstallable,
        isBrowserExtensionInstalled,
        isBrowserExtensionPopup,
        openSettingsPage,
        openAppInNewTab,
        openLinkInBackgroundTab,
    }
}
