import {
    ActionIcon,
    Anchor,
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
import { useViewportSize } from "@mantine/hooks"
import { Constants } from "app/constants"
import { redirectToAdd, redirectToRootCategory } from "app/slices/redirect"
import { reloadTree, setMobileMenuOpen } from "app/slices/tree"
import { reloadProfile, reloadSettings } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { Logo } from "components/Logo"
import { OnDesktop } from "components/responsive/OnDesktop"
import { OnMobile } from "components/responsive/OnMobile"
import { useAppLoading } from "hooks/useAppLoading"
import { LoadingPage } from "pages/LoadingPage"
import { ReactNode, useEffect } from "react"
import { TbPlus } from "react-icons/tb"
import { Outlet } from "react-router-dom"

interface LayoutProps {
    sidebar: ReactNode
    header: ReactNode
}

const sidebarPadding = DEFAULT_THEME.spacing.xs
const sidebarRightBorderWidth = 1

const useStyles = createStyles(theme => ({
    sidebarContent: {
        maxWidth: Constants.layout.sidebarWidth - sidebarPadding * 2 - sidebarRightBorderWidth,
        [theme.fn.smallerThan(Constants.layout.mobileBreakpoint)]: {
            maxWidth: `calc(100vw - ${sidebarPadding * 2 + sidebarRightBorderWidth}px)`,
        },
    },
    mainContentWrapper: {
        paddingTop: Constants.layout.headerHeight,
        paddingLeft: Constants.layout.sidebarWidth,
        paddingRight: 0,
        paddingBottom: 0,
        [theme.fn.smallerThan(Constants.layout.mobileBreakpoint)]: {
            paddingLeft: 0,
        },
    },
    mainContent: {
        maxWidth: `calc(100vw - ${Constants.layout.sidebarWidth}px)`,
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
        <Anchor onClick={() => dispatch(redirectToRootCategory())} variant="text">
            <Center inline>
                <Logo size={24} />
                <Title order={3} pl="md">
                    CommaFeed
                </Title>
            </Center>
        </Anchor>
    )
}

export default function Layout({ sidebar, header }: LayoutProps) {
    const { classes } = useStyles()
    const theme = useMantineTheme()
    const viewport = useViewportSize()
    const { loading } = useAppLoading()
    const mobileMenuOpen = useAppSelector(state => state.tree.mobileMenuOpen)
    const dispatch = useAppDispatch()

    useEffect(() => {
        dispatch(reloadSettings())
        dispatch(reloadProfile())
        dispatch(reloadTree())

        // reload tree periodically
        const id = setInterval(() => dispatch(reloadTree()), 30000)
        return () => clearInterval(id)
    }, [dispatch])

    const burger = (
        <Burger
            color={theme.fn.variant({ color: theme.primaryColor, variant: "subtle" }).color}
            opened={mobileMenuOpen}
            onClick={() => dispatch(setMobileMenuOpen(!mobileMenuOpen))}
            size="sm"
        />
    )

    if (loading) return <LoadingPage />
    return (
        <AppShell
            fixed
            navbarOffsetBreakpoint={Constants.layout.mobileBreakpoint}
            classNames={{ main: classes.mainContentWrapper }}
            navbar={
                <Navbar
                    p={sidebarPadding}
                    hiddenBreakpoint={Constants.layout.mobileBreakpoint}
                    hidden={!mobileMenuOpen}
                    width={{ md: Constants.layout.sidebarWidth }}
                >
                    <Navbar.Section grow component={ScrollArea} mx="-xs" px="xs">
                        <Box className={classes.sidebarContent}>{sidebar}</Box>
                    </Navbar.Section>
                </Navbar>
            }
            header={
                <Header height={Constants.layout.headerHeight} p="md">
                    <OnMobile>
                        {mobileMenuOpen && (
                            <Group position="apart">
                                <Box>{burger}</Box>
                                <Box>
                                    <LogoAndTitle />
                                </Box>
                                <Box>
                                    <ActionIcon color={theme.primaryColor} onClick={() => dispatch(redirectToAdd())}>
                                        <TbPlus size={18} />
                                    </ActionIcon>
                                </Box>
                            </Group>
                        )}
                        {!mobileMenuOpen && (
                            <Group>
                                <Box mr="sm">{burger}</Box>
                                <Box sx={{ flexGrow: 1 }}>{header}</Box>
                            </Group>
                        )}
                    </OnMobile>
                    <OnDesktop>
                        <Group>
                            <Group position="apart" sx={{ width: Constants.layout.sidebarWidth - 16 }}>
                                <Box>
                                    <LogoAndTitle />
                                </Box>
                                <Box>
                                    <ActionIcon color={theme.primaryColor} onClick={() => dispatch(redirectToAdd())}>
                                        <TbPlus size={18} />
                                    </ActionIcon>
                                </Box>
                            </Group>
                            <Box sx={{ flexGrow: 1 }}>{header}</Box>
                        </Group>
                    </OnDesktop>
                </Header>
            }
        >
            <ScrollArea
                sx={{ height: viewport.height - Constants.layout.headerHeight }}
                viewportRef={ref => {
                    if (ref) ref.id = Constants.dom.mainScrollAreaId
                }}
            >
                <Box className={classes.mainContent}>
                    <Outlet />
                </Box>
            </ScrollArea>
        </AppShell>
    )
}
