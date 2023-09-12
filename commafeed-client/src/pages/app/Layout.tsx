import {
    ActionIcon,
    AppShell,
    Box,
    Burger,
    Center,
    createStyles,
    DEFAULT_THEME,
    Group,
    Header,
    Navbar,
    ScrollArea,
    Title,
    useMantineTheme,
} from "@mantine/core"
import { Constants } from "app/constants"
import { redirectToAdd, redirectToRootCategory } from "app/slices/redirect"
import { reloadTree, setMobileMenuOpen, setSidebarWidth } from "app/slices/tree"
import { reloadProfile, reloadSettings, reloadTags } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { AnnouncementDialog } from "components/AnnouncementDialog"
import { Loader } from "components/Loader"
import { Logo } from "components/Logo"
import { OnDesktop } from "components/responsive/OnDesktop"
import { OnMobile } from "components/responsive/OnMobile"
import { useAppLoading } from "hooks/useAppLoading"
import { useMobile } from "hooks/useMobile"
import { useWebSocket } from "hooks/useWebSocket"
import { LoadingPage } from "pages/LoadingPage"
import { Resizable } from "re-resizable"
import { ReactNode, Suspense, useEffect } from "react"
import { TbPlus } from "react-icons/tb"
import { Outlet } from "react-router-dom"

interface LayoutProps {
    sidebar: ReactNode
    sidebarWidth: number
    header: ReactNode
}

const sidebarPadding = DEFAULT_THEME.spacing.xs
const sidebarRightBorderWidth = "1px"

const useStyles = createStyles((theme, props: LayoutProps) => ({
    sidebar: {
        "& .mantine-ScrollArea-scrollbar[data-orientation='horizontal']": {
            display: "none",
        },
    },
    sidebarContentResizeWrapper: {
        padding: sidebarPadding,
        minHeight: `calc(100vh - ${Constants.layout.headerHeight}px)`,
    },
    sidebarContent: {
        maxWidth: `calc(${props.sidebarWidth}px - ${sidebarPadding} * 2 - ${sidebarRightBorderWidth})`,
        [theme.fn.smallerThan(Constants.layout.mobileBreakpoint)]: {
            maxWidth: `calc(100vw - ${sidebarPadding} * 2 - ${sidebarRightBorderWidth})`,
        },
    },
    mainContentWrapper: {
        paddingTop: Constants.layout.headerHeight,
        paddingLeft: props.sidebarWidth,
        paddingRight: 0,
        paddingBottom: 0,
        [theme.fn.smallerThan(Constants.layout.mobileBreakpoint)]: {
            paddingLeft: 0,
        },
    },
    mainContent: {
        maxWidth: `calc(100vw - ${props.sidebarWidth}px)`,
        padding: theme.spacing.md,
        [theme.fn.smallerThan(Constants.layout.mobileBreakpoint)]: {
            maxWidth: "100vw",
            padding: "6px",
        },
    },
}))

function LogoAndTitle() {
    const dispatch = useAppDispatch()
    return (
        <Center inline onClick={() => dispatch(redirectToRootCategory())} style={{ cursor: "pointer" }}>
            <Logo size={24} />
            <Title order={3} pl="md">
                CommaFeed
            </Title>
        </Center>
    )
}

export default function Layout(props: LayoutProps) {
    const { classes } = useStyles(props)
    const theme = useMantineTheme()
    const { loading } = useAppLoading()
    const mobile = useMobile()
    const mobileMenuOpen = useAppSelector(state => state.tree.mobileMenuOpen)
    const webSocketConnected = useAppSelector(state => state.server.webSocketConnected)
    const sidebarHidden = props.sidebarWidth === 0
    const dispatch = useAppDispatch()
    useWebSocket()

    const handleResize = (element: HTMLElement) => dispatch(setSidebarWidth(element.offsetWidth))

    useEffect(() => {
        dispatch(reloadSettings())
        dispatch(reloadProfile())
        dispatch(reloadTree())
        dispatch(reloadTags())

        // reload tree periodically if not receiving websocket events
        const id = setInterval(() => {
            if (!webSocketConnected) dispatch(reloadTree())
        }, 30000)
        return () => clearInterval(id)
    }, [dispatch, webSocketConnected])

    const burger = (
        <Center>
            <Burger
                color={theme.fn.variant({ color: theme.primaryColor, variant: "subtle" }).color}
                opened={mobileMenuOpen}
                onClick={() => dispatch(setMobileMenuOpen(!mobileMenuOpen))}
                size="sm"
            />
        </Center>
    )

    const addButton = (
        <ActionIcon color={theme.primaryColor} onClick={() => dispatch(redirectToAdd())} aria-label="Subscribe">
            <TbPlus size={18} />
        </ActionIcon>
    )

    if (loading) return <LoadingPage />
    return (
        <AppShell
            fixed
            navbarOffsetBreakpoint={Constants.layout.mobileBreakpoint}
            classNames={{ main: classes.mainContentWrapper }}
            navbar={
                <Navbar
                    id="sidebar"
                    hiddenBreakpoint={sidebarHidden ? 99999999 : Constants.layout.mobileBreakpoint}
                    hidden={sidebarHidden || !mobileMenuOpen}
                    width={{ md: props.sidebarWidth }}
                    className={classes.sidebar}
                >
                    <Navbar.Section grow component={ScrollArea} mx={mobile ? 0 : "-sm"} px={mobile ? 0 : "sm"}>
                        <Resizable
                            enable={{
                                top: false,
                                right: !mobile,
                                bottom: false,
                                left: false,
                                topRight: false,
                                bottomRight: false,
                                bottomLeft: false,
                                topLeft: false,
                            }}
                            onResize={(e, dir, el) => handleResize(el)}
                            minWidth={120}
                            className={classes.sidebarContentResizeWrapper}
                        >
                            <Box className={classes.sidebarContent}>{props.sidebar}</Box>
                        </Resizable>
                    </Navbar.Section>
                </Navbar>
            }
            header={
                <Header id="header" height={Constants.layout.headerHeight} p="md">
                    <OnMobile>
                        {mobileMenuOpen && (
                            <Group position="apart">
                                <Box>{burger}</Box>
                                <Box>
                                    <LogoAndTitle />
                                </Box>
                                <Box>{addButton}</Box>
                            </Group>
                        )}
                        {!mobileMenuOpen && (
                            <Group>
                                <Box>{burger}</Box>
                                <Box sx={{ flexGrow: 1 }}>{props.header}</Box>
                            </Group>
                        )}
                    </OnMobile>
                    <OnDesktop>
                        <Group>
                            <Group position="apart" sx={{ width: props.sidebarWidth - 16 }}>
                                <Box>
                                    <LogoAndTitle />
                                </Box>
                                <Box>{addButton}</Box>
                            </Group>
                            <Box sx={{ flexGrow: 1 }}>{props.header}</Box>
                        </Group>
                    </OnDesktop>
                </Header>
            }
        >
            <Box id="content" className={classes.mainContent}>
                <Suspense fallback={<Loader />}>
                    <AnnouncementDialog />
                    <Outlet />
                </Suspense>
            </Box>
        </AppShell>
    )
}
