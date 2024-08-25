import { i18n } from "@lingui/core"
import { I18nProvider } from "@lingui/react"
import { MantineProvider } from "@mantine/core"
import { ModalsProvider } from "@mantine/modals"
import { Notifications } from "@mantine/notifications"
import { Constants } from "app/constants"
import { redirectTo } from "app/redirect/slice"
import { reloadServerInfos } from "app/server/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import { categoryUnreadCount } from "app/utils"
import { DisablePullToRefresh } from "components/DisablePullToRefresh"
import { ErrorBoundary } from "components/ErrorBoundary"
import { Header } from "components/header/Header"
import { Tree } from "components/sidebar/Tree"
import { useBrowserExtension } from "hooks/useBrowserExtension"
import { useI18n } from "i18n"
import { WelcomePage } from "pages/WelcomePage"
import { AdminUsersPage } from "pages/admin/AdminUsersPage"
import { MetricsPage } from "pages/admin/MetricsPage"
import { AboutPage } from "pages/app/AboutPage"
import { AddPage } from "pages/app/AddPage"
import { CategoryDetailsPage } from "pages/app/CategoryDetailsPage"
import { DonatePage } from "pages/app/DonatePage"
import { FeedDetailsPage } from "pages/app/FeedDetailsPage"
import { FeedEntriesPage } from "pages/app/FeedEntriesPage"
import Layout from "pages/app/Layout"
import { SettingsPage } from "pages/app/SettingsPage"
import { TagDetailsPage } from "pages/app/TagDetailsPage"
import { LoginPage } from "pages/auth/LoginPage"
import { PasswordRecoveryPage } from "pages/auth/PasswordRecoveryPage"
import { RegistrationPage } from "pages/auth/RegistrationPage"
import React, { useEffect } from "react"
import { isSafari } from "react-device-detect"
import ReactGA from "react-ga4"
import { Helmet } from "react-helmet"
import { HashRouter, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom"
import Tinycon from "tinycon"

function Providers(props: { children: React.ReactNode }) {
    return (
        <I18nProvider i18n={i18n}>
            <MantineProvider
                defaultColorScheme="auto"
                theme={{
                    primaryColor: "orange",
                    fontFamily: "Open Sans",
                    colors: {
                        // keep using dark colors from mantine v6
                        // https://v6.mantine.dev/theming/colors/#default-colors
                        dark: [
                            "#C1C2C5",
                            "#A6A7AB",
                            "#909296",
                            "#5c5f66",
                            "#373A40",
                            "#2C2E33",
                            "#25262b",
                            "#1A1B1E",
                            "#141517",
                            "#101113",
                        ],
                    },
                }}
            >
                <ModalsProvider>
                    <Notifications position="bottom-right" zIndex={9999} />
                    <ErrorBoundary>{props.children}</ErrorBoundary>
                </ModalsProvider>
            </MantineProvider>
        </I18nProvider>
    )
}

// api documentation page is very large, load only on-demand
const ApiDocumentationPage = React.lazy(async () => await import("pages/app/ApiDocumentationPage"))

function AppRoutes() {
    const sidebarVisible = useAppSelector(state => state.tree.sidebarVisible)

    return (
        <Routes>
            <Route path="/" element={<Navigate to={`/app/category/${Constants.categories.all.id}`} replace />} />
            <Route path="welcome" element={<WelcomePage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegistrationPage />} />
            <Route path="passwordRecovery" element={<PasswordRecoveryPage />} />
            <Route path="api" element={<ApiDocumentationPage />} />
            <Route path="app" element={<Layout header={<Header />} sidebar={<Tree />} sidebarVisible={sidebarVisible} />}>
                <Route path="category">
                    <Route path=":id" element={<FeedEntriesPage sourceType="category" />} />
                    <Route path=":id/details" element={<CategoryDetailsPage />} />
                </Route>
                <Route path="feed">
                    <Route path=":id" element={<FeedEntriesPage sourceType="feed" />} />
                    <Route path=":id/details" element={<FeedDetailsPage />} />
                </Route>
                <Route path="tag">
                    <Route path=":id" element={<FeedEntriesPage sourceType="tag" />} />
                    <Route path=":id/details" element={<TagDetailsPage />} />
                </Route>
                <Route path="add" element={<AddPage />} />
                <Route path="settings" element={<SettingsPage />} />
                <Route path="admin">
                    <Route path="users" element={<AdminUsersPage />} />
                    <Route path="metrics" element={<MetricsPage />} />
                </Route>
                <Route path="about" element={<AboutPage />} />
                <Route path="donate" element={<DonatePage />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    )
}

function RedirectHandler() {
    const target = useAppSelector(state => state.redirect.to)
    const dispatch = useAppDispatch()
    const navigate = useNavigate()
    useEffect(() => {
        if (target) {
            // pages can subscribe to state.timestamp in order to refresh when navigating to an url matching the current page
            navigate(target, { state: { timestamp: new Date() } })
            dispatch(redirectTo(undefined))
        }
    }, [target, dispatch, navigate])

    return null
}

function GoogleAnalyticsHandler() {
    const location = useLocation()
    const googleAnalyticsCode = useAppSelector(state => state.server.serverInfos?.googleAnalyticsCode)

    useEffect(() => {
        if (googleAnalyticsCode) ReactGA.initialize(googleAnalyticsCode)
    }, [googleAnalyticsCode])

    useEffect(() => {
        if (ReactGA.isInitialized) ReactGA.send({ hitType: "pageview", page: location.pathname })
    }, [location])

    return null
}

function UnreadCountTitleHandler({ unreadCount, enabled }: { unreadCount: number; enabled?: boolean }) {
    return <Helmet title={enabled && unreadCount > 0 ? `(${unreadCount}) CommaFeed` : "CommaFeed"} />
}

function UnreadCountFaviconHandler({ unreadCount, enabled }: { unreadCount: number; enabled?: boolean }) {
    useEffect(() => {
        if (enabled && unreadCount > 0) {
            Tinycon.setBubble(unreadCount)
        } else {
            Tinycon.reset()
        }
    }, [unreadCount, enabled])

    return null
}

function BrowserExtensionBadgeUnreadCountHandler() {
    const root = useAppSelector(state => state.tree.rootCategory)
    const { setBadgeUnreadCount } = useBrowserExtension()
    useEffect(() => {
        if (!root) return
        const unreadCount = categoryUnreadCount(root)
        setBadgeUnreadCount(unreadCount)
    }, [root, setBadgeUnreadCount])

    return null
}

function CustomCode() {
    return (
        <Helmet>
            <link rel="stylesheet" type="text/css" href="custom_css.css" />
            <script type="text/javascript" src="custom_js.js" />
        </Helmet>
    )
}

export function App() {
    useI18n()
    const root = useAppSelector(state => state.tree.rootCategory)
    const unreadCountTitle = useAppSelector(state => state.user.settings?.unreadCountTitle)
    const unreadCountFavicon = useAppSelector(state => state.user.settings?.unreadCountFavicon)
    const dispatch = useAppDispatch()

    const unreadCount = categoryUnreadCount(root)

    useEffect(() => {
        dispatch(reloadServerInfos())
    }, [dispatch])

    return (
        <Providers>
            <>
                <UnreadCountTitleHandler unreadCount={unreadCount} enabled={unreadCountTitle} />
                <UnreadCountFaviconHandler unreadCount={unreadCount} enabled={unreadCountFavicon} />
                <BrowserExtensionBadgeUnreadCountHandler />
                <HashRouter>
                    <GoogleAnalyticsHandler />
                    <RedirectHandler />
                    <AppRoutes />
                    <CustomCode />
                    {/* disable pull-to-refresh as it messes with vertical scrolling
                        safari behaves weirdly when overscroll-behavior is set to none so we disable it only for other browsers
                        https://github.com/Athou/commafeed/issues/1168
                    */}
                    {!isSafari && <DisablePullToRefresh />}
                </HashRouter>
            </>
        </Providers>
    )
}
