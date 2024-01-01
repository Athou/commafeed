import { Trans } from "@lingui/macro"
import { ActionIcon, AppShell, Box, Center, Group, ScrollArea, Title, useMantineTheme } from "@mantine/core"
import { Constants } from "app/constants"
import { redirectToAdd, redirectToRootCategory } from "app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import { setMobileMenuOpen } from "app/tree/slice"
import { reloadTree } from "app/tree/thunks"
import { reloadProfile, reloadSettings, reloadTags } from "app/user/thunks"
import { ActionButton } from "components/ActionButton"
import { AnnouncementDialog } from "components/AnnouncementDialog"
import { Loader } from "components/Loader"
import { Logo } from "components/Logo"
import { OnDesktop } from "components/responsive/OnDesktop"
import { OnMobile } from "components/responsive/OnMobile"
import { useAppLoading } from "hooks/useAppLoading"
import { useWebSocket } from "hooks/useWebSocket"
import { LoadingPage } from "pages/LoadingPage"
import { type ReactNode, Suspense, useEffect } from "react"
import Draggable from "react-draggable"
import { TbMenu2, TbPlus, TbX } from "react-icons/tb"
import { Outlet } from "react-router-dom"
import { tss } from "tss"
import useLocalStorage from "use-local-storage"

interface LayoutProps {
    sidebar: ReactNode
    sidebarVisible: boolean
    header: ReactNode
}

function LogoAndTitle() {
    const dispatch = useAppDispatch()
    return (
        <Center inline onClick={async () => await dispatch(redirectToRootCategory())} style={{ cursor: "pointer" }}>
            <Logo size={24} />
            <Title order={3} pl="md">
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

export default function Layout(props: LayoutProps) {
    const theme = useMantineTheme()
    const [sidebarWidth, setSidebarWidth] = useLocalStorage("sidebar-width", 350)
    const sidebarPadding = theme.spacing.xs
    const { classes } = useStyles({
        sidebarWidth,
        sidebarPadding,
        sidebarRightBorderWidth: "1px",
    })
    const { loading } = useAppLoading()
    const mobileMenuOpen = useAppSelector(state => state.tree.mobileMenuOpen)
    const webSocketConnected = useAppSelector(state => state.server.webSocketConnected)
    const treeReloadInterval = useAppSelector(state => state.server.serverInfos?.treeReloadInterval)
    const dispatch = useAppDispatch()
    useWebSocket()

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
            label={mobileMenuOpen ? <Trans>Close menu</Trans> : <Trans>Open menu</Trans>}
            icon={mobileMenuOpen ? <TbX size={18} /> : <TbMenu2 size={18} />}
            onClick={() => dispatch(setMobileMenuOpen(!mobileMenuOpen))}
        ></ActionButton>
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

    if (loading) return <LoadingPage />
    return (
        <AppShell
            header={{ height: Constants.layout.headerHeight }}
            navbar={{
                width: sidebarWidth,
                breakpoint: Constants.layout.mobileBreakpoint,
                collapsed: { mobile: !mobileMenuOpen, desktop: !props.sidebarVisible },
            }}
            padding={{ base: 6, [Constants.layout.mobileBreakpointName]: "md" }}
        >
            <AppShell.Header id="header">
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
            </AppShell.Header>
            <AppShell.Navbar id="sidebar" p={sidebarPadding}>
                <AppShell.Section grow component={ScrollArea} mx="-sm" px="sm">
                    <Box className={classes.sidebarContent}>{props.sidebar}</Box>
                </AppShell.Section>
            </AppShell.Navbar>
            <Draggable
                axis="x"
                defaultPosition={{
                    x: sidebarWidth,
                    y: Constants.layout.headerHeight,
                }}
                bounds={{
                    left: 120,
                    right: 1000,
                }}
                grid={[30, 30]}
                onDrag={(_e, data) => setSidebarWidth(data.x)}
            >
                <Box
                    style={{
                        position: "fixed",
                        height: "100%",
                        width: "10px",
                        cursor: "ew-resize",
                    }}
                ></Box>
            </Draggable>

            <AppShell.Main id="content">
                <Suspense fallback={<Loader />}>
                    <AnnouncementDialog />
                    <Outlet />
                </Suspense>
            </AppShell.Main>
        </AppShell>
    )
}
