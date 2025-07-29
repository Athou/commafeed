import { msg } from "@lingui/core/macro"
import { ActionIcon, AppShell, Box, Center, Group, ScrollArea, Title, useMantineTheme } from "@mantine/core"
import { type ReactNode, type RefObject, Suspense, useEffect, useRef } from "react"
import Draggable from "react-draggable"
import { TbMenu2, TbPlus, TbX } from "react-icons/tb"
import { Outlet } from "react-router-dom"
import { useSwipeable } from "react-swipeable"
import { Constants } from "@/app/constants"
import { redirectToAdd, redirectToRootCategory } from "@/app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import { setMobileMenuOpen } from "@/app/tree/slice"
import { reloadTree } from "@/app/tree/thunks"
import { setSidebarWidth } from "@/app/user/slice"
import { reloadProfile, reloadSettings, reloadTags } from "@/app/user/thunks"
import { ActionButton } from "@/components/ActionButton"
import { AnnouncementDialog } from "@/components/AnnouncementDialog"
import { Loader } from "@/components/Loader"
import { Logo } from "@/components/Logo"
import { MarkAllAsReadConfirmationDialog } from "@/components/MarkAllAsReadConfirmationDialog"
import { OnDesktop } from "@/components/responsive/OnDesktop"
import { OnMobile } from "@/components/responsive/OnMobile"
import { useAppLoading } from "@/hooks/useAppLoading"
import { useBrowserExtension } from "@/hooks/useBrowserExtension"
import { useMobile } from "@/hooks/useMobile"
import { useWebSocket } from "@/hooks/useWebSocket"
import { LoadingPage } from "@/pages/LoadingPage"
import { tss } from "@/tss"

interface LayoutProps {
    sidebar: ReactNode
    sidebarVisible: boolean
    header: ReactNode
}

function LogoAndTitle() {
    const dispatch = useAppDispatch()
    return (
        <Center
            className="cf-logo-title"
            inline
            onClick={async () => await dispatch(redirectToRootCategory())}
            style={{ cursor: "pointer" }}
        >
            <Box className="cf-logo">
                <Logo size={24} />
            </Box>
            <Title order={3} pl="md" className="cf-title">
                CommaFeed
            </Title>
        </Center>
    )
}

const useStyles = tss
    .withParams<{
        sidebarWidth: number
        sidebarPadding: string
        sidebarRightBorderWidth: string
    }>()
    .create(({ sidebarWidth, sidebarPadding, sidebarRightBorderWidth }) => {
        return {
            sidebarContent: {
                maxWidth: `calc(${sidebarWidth}px - ${sidebarPadding} * 2 - ${sidebarRightBorderWidth})`,
                [`@media (max-width: ${Constants.layout.mobileBreakpoint}px)`]: {
                    maxWidth: `calc(100vw - ${sidebarPadding} * 2 - ${sidebarRightBorderWidth})`,
                },
            },
        }
    })

export default function Layout(props: Readonly<LayoutProps>) {
    const theme = useMantineTheme()
    const mobile = useMobile()
    const { isBrowserExtensionPopup } = useBrowserExtension()
    const draggableSeparator = useRef<HTMLDivElement>(null)

    const { loading } = useAppLoading()
    const mobileMenuOpen = useAppSelector(state => state.tree.mobileMenuOpen)
    const webSocketConnected = useAppSelector(state => state.server.webSocketConnected)
    const treeReloadInterval = useAppSelector(state => state.server.serverInfos?.treeReloadInterval)
    const mobileFooter = useAppSelector(state => state.user.settings?.mobileFooter)
    const sidebarWidth = useAppSelector(state => state.user.localSettings.sidebarWidth)
    const headerInFooter = mobile && !isBrowserExtensionPopup && mobileFooter
    const dispatch = useAppDispatch()
    useWebSocket()

    const sidebarPadding = theme.spacing.xs
    const { classes } = useStyles({
        sidebarWidth,
        sidebarPadding,
        sidebarRightBorderWidth: "1px",
    })

    useEffect(() => {
        // load initial data
        dispatch(reloadSettings())
        dispatch(reloadProfile())
        dispatch(reloadTree())
        dispatch(reloadTags())
    }, [dispatch])

    useEffect(() => {
        let timer: number | undefined

        if (!webSocketConnected && treeReloadInterval) {
            // reload tree periodically if not receiving websocket events
            timer = window.setInterval(async () => await dispatch(reloadTree()), treeReloadInterval)
        }

        return () => clearInterval(timer)
    }, [dispatch, webSocketConnected, treeReloadInterval])

    const burger = (
        <ActionButton
            label={mobileMenuOpen ? msg`Close menu` : msg`Open menu`}
            icon={mobileMenuOpen ? <TbX size={18} /> : <TbMenu2 size={18} />}
            onClick={() => dispatch(setMobileMenuOpen(!mobileMenuOpen))}
        />
    )

    const addButton = (
        <ActionIcon
            color={theme.primaryColor}
            variant="subtle"
            onClick={async () => await dispatch(redirectToAdd())}
            aria-label="Subscribe"
        >
            <TbPlus size={18} />
        </ActionIcon>
    )

    const header = (
        <>
            <OnMobile>
                {mobileMenuOpen && (
                    <Group justify="space-between" p="md">
                        <Box>{burger}</Box>
                        <Box>
                            <LogoAndTitle />
                        </Box>
                        <Box>{addButton}</Box>
                    </Group>
                )}
                {!mobileMenuOpen && (
                    <Group p="md">
                        <Box>{burger}</Box>
                        <Box style={{ flexGrow: 1 }}>{props.header}</Box>
                    </Group>
                )}
            </OnMobile>
            <OnDesktop>
                <Group p="md">
                    <Group justify="space-between" style={{ width: sidebarWidth - 16 }}>
                        <Box>
                            <LogoAndTitle />
                        </Box>
                        <Box>{addButton}</Box>
                    </Group>
                    <Box style={{ flexGrow: 1 }}>{props.header}</Box>
                </Group>
            </OnDesktop>
        </>
    )

    const swipeHandlers = useSwipeable({
        onSwiping: e => {
            const threshold = document.documentElement.clientWidth / 6
            if (e.absX > threshold) {
                dispatch(setMobileMenuOpen(e.dir === "Right"))
            }
        },
    })

    if (loading) return <LoadingPage />
    return (
        <Box {...swipeHandlers}>
            <AppShell
                header={{ height: Constants.layout.headerHeight, collapsed: headerInFooter }}
                footer={{ height: Constants.layout.headerHeight, collapsed: !headerInFooter }}
                navbar={{
                    width: sidebarWidth,
                    breakpoint: Constants.layout.mobileBreakpoint,
                    collapsed: { mobile: !mobileMenuOpen, desktop: !props.sidebarVisible },
                }}
                padding={{ base: 6, [Constants.layout.mobileBreakpointName]: "md" }}
            >
                <AppShell.Header>{!headerInFooter && header}</AppShell.Header>
                <AppShell.Footer>{headerInFooter && header}</AppShell.Footer>
                <AppShell.Navbar p={sidebarPadding}>
                    <AppShell.Section grow component={ScrollArea} mx="-sm" px="sm">
                        <Box className={classes.sidebarContent}>{props.sidebar}</Box>
                    </AppShell.Section>
                </AppShell.Navbar>
                <OnDesktop>
                    <Draggable
                        nodeRef={draggableSeparator as RefObject<HTMLElement>}
                        axis="x"
                        defaultPosition={{
                            x: sidebarWidth,
                            y: 0,
                        }}
                        bounds={{
                            left: 120,
                            right: 1000,
                        }}
                        grid={[30, 30]}
                        onDrag={(_e, data) => {
                            dispatch(setSidebarWidth(data.x))
                        }}
                    >
                        <Box
                            ref={draggableSeparator}
                            style={{
                                position: "fixed",
                                height: "100%",
                                width: "10px",
                                cursor: "ew-resize",
                            }}
                        />
                    </Draggable>
                </OnDesktop>

                <AppShell.Main>
                    <Suspense fallback={<Loader />}>
                        <AnnouncementDialog />
                        <MarkAllAsReadConfirmationDialog />
                        <Outlet />
                    </Suspense>
                </AppShell.Main>
            </AppShell>
        </Box>
    )
}
