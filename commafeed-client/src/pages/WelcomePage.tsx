import { msg } from "@lingui/core/macro"
import { Anchor, Box, Center, Container, Divider, Group, Image, Space, Title, useMantineColorScheme } from "@mantine/core"
import { useAsyncCallback } from "react-async-hook"
import { SiGithub, SiX } from "react-icons/si"
import { TbClock, TbKey, TbMoon, TbSettings, TbSun, TbUserPlus } from "react-icons/tb"
import { client } from "@/app/client"
import { redirectToApiDocumentation, redirectToLogin, redirectToRegistration, redirectToRootCategory } from "@/app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import welcomePageDark from "@/assets/welcome_page_dark.png"
import welcomePageLight from "@/assets/welcome_page_light.png"
import { ActionButton } from "@/components/ActionButton"
import { useBrowserExtension } from "@/hooks/useBrowserExtension"
import { useMobile } from "@/hooks/useMobile"
import { PageTitle } from "./PageTitle"

const iconSize = 18

export function WelcomePage() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const { colorScheme } = useMantineColorScheme()
    const dispatch = useAppDispatch()
    const image = colorScheme === "light" ? welcomePageLight : welcomePageDark

    const login = useAsyncCallback(client.user.login, {
        onSuccess: () => {
            dispatch(redirectToRootCategory())
        },
    })

    return (
        <Container>
            <Header />

            <Center my="lg">
                <Title order={3}>Bloat-free feed reader</Title>
            </Center>

            {serverInfos?.demoAccountEnabled && (
                <Center>
                    <ActionButton
                        label={msg`Try the demo!`}
                        icon={<TbClock size={iconSize} />}
                        variant="outline"
                        onClick={async () => await login.execute({ name: "demo", password: "demo" })}
                        showLabelOnMobile
                    />
                </Center>
            )}

            <Divider my="lg" />

            <Image src={image} />

            <Divider my="lg" />

            <Footer />

            <Space h="lg" />
        </Container>
    )
}

function Header() {
    const mobile = useMobile()

    if (mobile) {
        return (
            <>
                <PageTitle />
                <Center>
                    <Buttons />
                </Center>
            </>
        )
    }

    return (
        <Group justify="space-between">
            <Box>
                <PageTitle />
            </Box>
            <Box>
                <Buttons />
            </Box>
        </Group>
    )
}

function Buttons() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const { colorScheme, toggleColorScheme } = useMantineColorScheme()
    const { isBrowserExtensionPopup, openSettingsPage } = useBrowserExtension()
    const dispatch = useAppDispatch()
    const dark = colorScheme === "dark"

    return (
        <Group gap={14}>
            <ActionButton
                label={msg`Log in`}
                icon={<TbKey size={iconSize} />}
                variant="outline"
                onClick={async () => await dispatch(redirectToLogin())}
                showLabelOnMobile
            />
            {serverInfos?.allowRegistrations && (
                <ActionButton
                    label={msg`Sign up`}
                    icon={<TbUserPlus size={iconSize} />}
                    variant="filled"
                    onClick={async () => await dispatch(redirectToRegistration())}
                    showLabelOnMobile
                />
            )}

            <ActionButton
                label={dark ? msg`Switch to light theme` : msg`Switch to dark theme`}
                icon={colorScheme === "dark" ? <TbSun size={18} /> : <TbMoon size={iconSize} />}
                onClick={() => toggleColorScheme()}
                hideLabelOnDesktop
            />

            {isBrowserExtensionPopup && (
                <ActionButton
                    label={msg`Extension options`}
                    icon={<TbSettings size={iconSize} />}
                    onClick={() => openSettingsPage()}
                    hideLabelOnDesktop
                />
            )}
        </Group>
    )
}

function Footer() {
    const dispatch = useAppDispatch()
    return (
        <Group justify="space-between">
            <Group>
                <span>© CommaFeed</span>
                <Anchor variant="text" href="https://github.com/Athou/commafeed/" target="_blank" rel="noreferrer">
                    <SiGithub />
                </Anchor>
                <Anchor variant="text" href="https://x.com/CommaFeed" target="_blank" rel="noreferrer">
                    <SiX />
                </Anchor>
            </Group>
            <Box>
                <Anchor variant="text" onClick={async () => await dispatch(redirectToApiDocumentation())}>
                    API documentation
                </Anchor>
            </Box>
        </Group>
    )
}
