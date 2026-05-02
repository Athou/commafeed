import { i18n } from "@lingui/core"
import { I18nProvider } from "@lingui/react"
import { MantineProvider, v8CssVariablesResolver } from "@mantine/core"
import { ModalsProvider } from "@mantine/modals"
import { Notifications } from "@mantine/notifications"
import type React from "react"
import { useEffect } from "react"
import { HashRouter, Navigate, Route, Routes, useNavigate } from "react-router-dom"
import { Constants } from "@/app/constants"
import { redirectTo } from "@/app/redirect/slice"
import { redirectToInitialSetup } from "@/app/redirect/thunks"
import { reloadServerInfos } from "@/app/server/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import { ErrorBoundary } from "@/components/ErrorBoundary"
import { Header } from "@/components/header/Header"
import { Tree } from "@/components/sidebar/Tree"
import { useI18n } from "@/i18n"
import { AdminUsersPage } from "@/pages/admin/AdminUsersPage"
import { MetricsPage } from "@/pages/admin/MetricsPage"
import { AboutPage } from "@/pages/app/AboutPage"
import { AddPage } from "@/pages/app/AddPage"
import { CategoryDetailsPage } from "@/pages/app/CategoryDetailsPage"
import { DonatePage } from "@/pages/app/DonatePage"
import { FeedDetailsPage } from "@/pages/app/FeedDetailsPage"
import { FeedEntriesPage } from "@/pages/app/FeedEntriesPage"
import Layout from "@/pages/app/Layout"
import { SettingsPage } from "@/pages/app/SettingsPage"
import { TagDetailsPage } from "@/pages/app/TagDetailsPage"
import { InitialSetupPage } from "@/pages/auth/InitialSetupPage"
import { LoginPage } from "@/pages/auth/LoginPage"
import { PasswordRecoveryPage } from "@/pages/auth/PasswordRecoveryPage"
import { PasswordResetPage } from "@/pages/auth/PasswordResetPage"
import { RegistrationPage } from "@/pages/auth/RegistrationPage"
import { WelcomePage } from "@/pages/WelcomePage"

function Providers(
    props: Readonly<{
        children: React.ReactNode
    }>
) {
    const primaryColor = useAppSelector(state => state.user.settings?.primaryColor) || Constants.theme.defaultPrimaryColor
    return (
        <I18nProvider i18n={i18n}>
            <MantineProvider
                defaultColorScheme="auto"
                // keep using css variables from mantine v8
                cssVariablesResolver={v8CssVariablesResolver}
                theme={{
                    primaryColor: primaryColor,
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

function AppRoutes() {
    const sidebarVisible = useAppSelector(state => state.tree.sidebarVisible)

    return (
        <Routes>
            <Route path="/" element={<Navigate to={`/app/category/${Constants.categories.all.id}`} replace />} />
            <Route path="welcome" element={<WelcomePage />} />
            <Route path="setup" element={<InitialSetupPage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegistrationPage />} />
            <Route path="passwordRecovery" element={<PasswordRecoveryPage />} />
            <Route path="passwordReset" element={<PasswordResetPage />} />
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

function InitialSetupHandler() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const dispatch = useAppDispatch()
    useEffect(() => {
        if (serverInfos?.initialSetupRequired) {
            dispatch(redirectToInitialSetup())
        }
    }, [serverInfos, dispatch])

    return null
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

export function App() {
    useI18n()
    const dispatch = useAppDispatch()

    useEffect(() => {
        dispatch(reloadServerInfos())
    }, [dispatch])

    return (
        <Providers>
            <HashRouter>
                <InitialSetupHandler />
                <RedirectHandler />
                <AppRoutes />
            </HashRouter>
        </Providers>
    )
}
