import { Trans } from "@lingui/macro"
import { Anchor, Box, Center, Container, Divider, Group, Image, Title, useMantineColorScheme } from "@mantine/core"
import { useMediaQuery } from "@mantine/hooks"
import { client } from "app/client"
import { Constants } from "app/constants"
import { redirectToLogin, redirectToRegistration, redirectToRootCategory } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import welcome_page_dark from "assets/welcome_page_dark.png"
import welcome_page_light from "assets/welcome_page_light.png"
import { ActionButton } from "components/ActionButtton"
import { ButtonToolbar } from "components/ButtonToolbar"
import { useAsyncCallback } from "react-async-hook"
import { SiGithub, TbKey, TbUserPlus } from "react-icons/all"
import { SiTwitter } from "react-icons/si"
import { TbClock, TbMoon, TbSun } from "react-icons/tb"
import { PageTitle } from "./PageTitle"

export function WelcomePage() {
    const { colorScheme } = useMantineColorScheme()
    const image = colorScheme === "light" ? welcome_page_light : welcome_page_dark
    return (
        <Container>
            <Header />

            <Center my="xl">
                <Title order={3}>Bloat-free feed reader</Title>
            </Center>

            <Divider my="xl" />

            <Image src={image} />

            <Divider my="xl" />

            <Footer />
        </Container>
    )
}

function Header() {
    const mobile = !useMediaQuery(`(min-width: ${Constants.layout.mobileBreakpoint}px)`)

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
    const iconSize = 18
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const { colorScheme, toggleColorScheme } = useMantineColorScheme()

    const dispatch = useAppDispatch()

    const login = useAsyncCallback(client.user.login, {
        onSuccess: () => {
            dispatch(redirectToRootCategory())
        },
    })

    return (
        <ButtonToolbar>
            {serverInfos?.demoAccountEnabled && (
                <ActionButton
                    label={<Trans>Try the demo!</Trans>}
                    icon={<TbClock size={iconSize} />}
                    variant="outline"
                    onClick={() => login.execute({ name: "demo", password: "demo" })}
                    showLabelOnMobile
                />
            )}
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
                icon={colorScheme === "dark" ? <TbSun size={18} /> : <TbMoon size={iconSize} />}
                onClick={() => toggleColorScheme()}
            />
        </ButtonToolbar>
    )
}

function Footer() {
    return (
        <Box>
            <Group>
                <span>© CommaFeed</span>
                <span> - </span>
                <Anchor variant="text" href="https://github.com/Athou/commafeed/" target="_blank" rel="noreferrer">
                    <SiGithub />
                </Anchor>
                <Anchor variant="text" href="https://twitter.com/CommaFeed" target="_blank" rel="noreferrer">
                    <SiTwitter />
                </Anchor>
            </Group>
        </Box>
    )
}
