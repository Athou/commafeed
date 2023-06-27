import { Trans } from "@lingui/macro"
import { Anchor, Box, Center, Container, Divider, Group, Image, Title, useMantineColorScheme } from "@mantine/core"
import { client } from "app/client"
import { redirectToApiDocumentation, redirectToLogin, redirectToRegistration, redirectToRootCategory } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import welcome_page_dark from "assets/welcome_page_dark.png"
import welcome_page_light from "assets/welcome_page_light.png"
import { ActionButton } from "components/ActionButton"
import { useBrowserExtension } from "hooks/useBrowserExtension"
import { useMobile } from "hooks/useMobile"
import { useAsyncCallback } from "react-async-hook"
import { SiGithub, SiTwitter } from "react-icons/si"
import { TbClock, TbKey, TbMoon, TbSettings, TbSun, TbUserPlus } from "react-icons/tb"
import { PageTitle } from "./PageTitle"

const iconSize = 18

export function WelcomePage() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const { colorScheme } = useMantineColorScheme()
    const dispatch = useAppDispatch()
    const image = colorScheme === "light" ? welcome_page_light : welcome_page_dark

    const login = useAsyncCallback(client.user.login, {
        onSuccess: () => {
            dispatch(redirectToRootCategory())
        },
    })

    return (
        <Container>
            <Header />

            <Center my="xl">
                <Title order={3}>Bloat-free feed reader</Title>
            </Center>

            {serverInfos?.demoAccountEnabled && (
                <Center>
                    <ActionButton
                        label={<Trans>Try the demo!</Trans>}
                        icon={<TbClock size={iconSize} />}
                        variant="outline"
                        onClick={() => login.execute({ name: "demo", password: "demo" })}
                        showLabelOnMobile
                    />
                </Center>
            )}

            <Divider my="xl" />

            <Image src={image} />

            <Divider my="xl" />

            <Footer />
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
        <Group position="apart">
            <PageTitle />
            <Buttons />
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
        <Group spacing={14}>
            <ActionButton
                label={<Trans>Log in</Trans>}
                icon={<TbKey size={iconSize} />}
                variant="outline"
                onClick={() => dispatch(redirectToLogin())}
                showLabelOnMobile
            />
            {serverInfos?.allowRegistrations && (
                <ActionButton
                    label={<Trans>Sign up</Trans>}
                    icon={<TbUserPlus size={iconSize} />}
                    variant="filled"
                    onClick={() => dispatch(redirectToRegistration())}
                    showLabelOnMobile
                />
            )}

            <ActionButton
                label={dark ? <Trans>Switch to light theme</Trans> : <Trans>Switch to dark theme</Trans>}
                icon={colorScheme === "dark" ? <TbSun size={18} /> : <TbMoon size={iconSize} />}
                onClick={() => toggleColorScheme()}
                hideLabelOnDesktop
            />

            {isBrowserExtensionPopup && (
                <ActionButton
                    label={<Trans>Extension options</Trans>}
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
        <Group position="apart">
            <Group>
                <span>© CommaFeed</span>
                <Anchor variant="text" href="https://github.com/Athou/commafeed/" target="_blank" rel="noreferrer">
                    <SiGithub />
                </Anchor>
                <Anchor variant="text" href="https://twitter.com/CommaFeed" target="_blank" rel="noreferrer">
                    <SiTwitter />
                </Anchor>
            </Group>
            <Box>
                <Anchor variant="text" onClick={() => dispatch(redirectToApiDocumentation())}>
                    API documentation
                </Anchor>
            </Box>
        </Group>
    )
}
