import { MantineProvider } from "@mantine/core"
import { act, render, screen } from "@testing-library/react"
import type { ReactNode } from "react"
import { beforeEach, describe, expect, it, vi } from "vitest"
import type { RootState } from "@/app/store"
import Layout from "./Layout"

const { appState, useMobile } = vi.hoisted(() => ({
    appState: {
        server: {
            webSocketConnected: true,
            serverInfos: undefined,
        },
        tree: {
            mobileMenuOpen: false,
            rootCategory: undefined,
        },
        user: {
            localSettings: {
                sidebarWidth: 360,
            },
            settings: {
                disablePullToRefresh: false,
                mobileFooter: false,
                unreadCountFavicon: false,
                unreadCountTitle: false,
            },
        },
    },
    useMobile: vi.fn(),
}))

vi.mock(import("@/app/store"), () => ({
    useAppDispatch: () => vi.fn(),
    useAppSelector: <Selected,>(selector: (state: RootState) => Selected) => selector(appState as unknown as RootState),
}))
vi.mock(import("@/components/ActionButton"), () => ({
    ActionButton: ({ label }: { label: unknown }) => <button type="button">{String(label)}</button>,
}))
vi.mock(import("@/components/AnnouncementDialog"), () => ({ AnnouncementDialog: () => null }))
vi.mock(import("@/components/DisablePullToRefresh"), () => ({ DisablePullToRefresh: () => null }))
vi.mock(import("@/components/Logo"), () => ({ Logo: () => <span>Logo</span> }))
vi.mock(import("@/components/MarkAllAsReadConfirmationDialog"), () => ({
    MarkAllAsReadConfirmationDialog: () => null,
}))
vi.mock(import("@/hooks/useAppLoading"), () => ({
    useAppLoading: () => ({ loading: false, loadingPercentage: 100, loadingStepLabel: undefined, steps: [] }),
}))
vi.mock(import("@/hooks/useBrowserExtension"), () => ({
    useBrowserExtension: () => ({
        browserExtensionVersion: null,
        isBrowserExtensionInstallable: true,
        isBrowserExtensionInstalled: false,
        isBrowserExtensionPopup: false,
        openAppInNewTab: vi.fn(),
        openLinkInBackgroundTab: vi.fn(),
        openSettingsPage: vi.fn(),
        setBadgeUnreadCount: vi.fn(),
    }),
}))
vi.mock(import("@/hooks/useMobile"), () => ({ useMobile }))
vi.mock(import("@/hooks/useWebSocket"), () => ({ useWebSocket: vi.fn() }))
vi.mock(import("react-draggable"), () => ({
    default: ({ children }: { children: ReactNode }) => children,
}))
vi.mock(import("react-router-dom"), () => ({ Outlet: () => null }))
vi.mock(import("react-swipeable"), () => ({ useSwipeable: () => ({}) }))
vi.mock(import("tinycon"), () => ({
    default: { reset: vi.fn(), setBubble: vi.fn(), setOptions: vi.fn() },
}))

vi.stubGlobal(
    "ResizeObserver",
    class {
        disconnect() {}
        observe() {}
        unobserve() {}
    }
)

const renderLayout = () =>
    render(<Layout sidebar={<div>Sidebar</div>} sidebarVisible header={<div>Toolbar</div>} />, {
        wrapper: MantineProvider,
    })

const groupFor = (text: string) => {
    const group = screen.getByText(text).closest(".mantine-Group-root")
    expect(group).not.toBeNull()
    return group as HTMLElement
}

describe("Layout", () => {
    beforeEach(() => {
        appState.tree.mobileMenuOpen = false
        appState.user.localSettings.sidebarWidth = 360
        appState.user.settings.mobileFooter = false
        useMobile.mockReturnValue(false)
        Object.defineProperty(window, "innerWidth", { configurable: true, value: 1280, writable: true })
    })

    it("keeps the desktop header row and sidebar segment from wrapping", () => {
        renderLayout()

        const sidebarGroup = groupFor("CommaFeed")
        const headerGroup = sidebarGroup.parentElement

        expect(sidebarGroup).toHaveStyle({ flexShrink: "0", width: "344px" })
        expect(sidebarGroup.style.getPropertyValue("--group-wrap")).toBe("nowrap")
        expect(headerGroup?.style.getPropertyValue("--group-wrap")).toBe("nowrap")
        const toolbarGroup = groupFor("Toolbar")
        expect(toolbarGroup).toBe(headerGroup)
        expect(screen.getByText("Toolbar").parentElement).toHaveStyle({ flexGrow: "1", minWidth: "0" })
    })

    it("keeps the toolbar in the desktop header row when the viewport loses scrollbar width", () => {
        renderLayout()
        const headerGroup = groupFor("Toolbar")

        act(() => {
            Object.defineProperty(window, "innerWidth", { configurable: true, value: 1263, writable: true })
            window.dispatchEvent(new Event("resize"))
        })

        expect(groupFor("Toolbar")).toBe(headerGroup)
        expect(headerGroup.style.getPropertyValue("--group-wrap")).toBe("nowrap")
    })

    it("does not apply the desktop constraints to the mobile menu in the footer", () => {
        appState.tree.mobileMenuOpen = true
        appState.user.settings.mobileFooter = true
        useMobile.mockReturnValue(true)

        renderLayout()

        const mobileGroup = groupFor("CommaFeed")
        expect(mobileGroup.closest("footer")).not.toBeNull()
        expect(mobileGroup.style.getPropertyValue("--group-wrap")).not.toBe("nowrap")
        expect(mobileGroup).not.toHaveStyle({ width: "344px" })
    })
})
