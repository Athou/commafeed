export const useBrowserExtension = () => {
    // when not in an iframe, window.parent is a reference to window
    const isBrowserExtension = window.parent !== window

    const openSettingsPage = () => window.parent.postMessage("open-settings-page", "*")
    const openAppInNewTab = () => window.parent.postMessage("open-app-in-new-tab", "*")

    return { isBrowserExtension, openSettingsPage, openAppInNewTab }
}
