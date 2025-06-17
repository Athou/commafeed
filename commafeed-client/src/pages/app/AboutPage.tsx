import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Container, List, NativeSelect, SimpleGrid, Title } from "@mantine/core"
import { Constants } from "app/constants"
import { redirectToApiDocumentation } from "app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import { CategorySelect } from "components/content/add/CategorySelect"
import { KeyboardShortcutsHelp } from "components/KeyboardShortcutsHelp"
import { useBrowserExtension } from "hooks/useBrowserExtension"
import type React from "react"
import { useState } from "react"
import { TbHelp, TbKeyboard, TbPuzzle, TbRocket } from "react-icons/tb"
import { tss } from "tss"

const useStyles = tss.create(() => ({
    sectionTitle: {
        display: "flex",
        alignItems: "center",
    },
}))

function Section(props: { title: React.ReactNode; icon: React.ReactNode; children: React.ReactNode }) {
    const { classes } = useStyles()
    return (
        <Box my="xl">
            <Box className={classes.sectionTitle} mb="xs">
                {props.icon}
                <Title order={3} ml="xs">
                    {props.title}
                </Title>
            </Box>
            <Box>{props.children}</Box>
        </Box>
    )
}

function NextUnreadBookmarklet() {
    const [categoryId, setCategoryId] = useState(Constants.categories.all.id)
    const [order, setOrder] = useState("desc")
    const { _ } = useLingui()

    const baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf("#"))
    const href = `${baseUrl}next?category=${categoryId}&order=${order}`

    return (
        <Box>
            <CategorySelect value={categoryId} onChange={c => c && setCategoryId(c)} withAll description={<Trans>Category</Trans>} />
            <NativeSelect
                data={[
                    { value: "desc", label: _(msg`Newest first`) },
                    { value: "asc", label: _(msg`Oldest first`) },
                ]}
                value={order}
                onChange={e => setOrder(e.target.value)}
                description={<Trans>Order</Trans>}
            />
            <Trans>Drag link to bookmark bar</Trans>
            <span> </span>
            <Anchor href={href} target="_blank" rel="noreferrer">
                <Trans>CommaFeed next unread item</Trans>
            </Anchor>
        </Box>
    )
}

export function AboutPage() {
    const version = useAppSelector(state => state.server.serverInfos?.version)
    const revision = useAppSelector(state => state.server.serverInfos?.gitCommit)
    const { isBrowserExtensionInstalled, browserExtensionVersion, isBrowserExtensionInstallable } = useBrowserExtension()
    const dispatch = useAppDispatch()

    return (
        <Container size="xl">
            <SimpleGrid cols={{ base: 1, [Constants.layout.mobileBreakpointName]: 2 }}>
                <Section title={<Trans>About</Trans>} icon={<TbHelp size={24} />}>
                    <Box>
                        <Trans>
                            CommaFeed version {version} ({revision}).
                        </Trans>
                    </Box>
                    {isBrowserExtensionInstallable && isBrowserExtensionInstalled && (
                        <Box>
                            <Trans>CommaFeed browser extension version {browserExtensionVersion}.</Trans>
                        </Box>
                    )}
                    <Box mt="md">
                        <Trans>
                            <span>CommaFeed is an open-source project. Sources are hosted on </span>
                            <Anchor href="https://github.com/Athou/commafeed" target="_blank" rel="noreferrer">
                                GitHub
                            </Anchor>
                            .
                        </Trans>
                    </Box>
                    <Box>
                        <Trans>If you encounter an issue, please report it on the issues page of the GitHub project.</Trans>
                    </Box>
                </Section>
                <Section title={<Trans>Goodies</Trans>} icon={<TbPuzzle size={24} />}>
                    <List>
                        <List.Item>
                            <Anchor href={Constants.browserExtensionUrl} target="_blank" rel="noreferrer">
                                <Trans>Browser extention</Trans>
                            </Anchor>
                        </List.Item>
                        <List.Item>
                            <Trans>Subscribe URL</Trans>
                            <span> </span>
                            <Anchor href="rest/feed/subscribe?url=FEED_URL_HERE" target="_blank" rel="noreferrer">
                                rest/feed/subscribe?url=FEED_URL_HERE
                            </Anchor>
                        </List.Item>
                        <List.Item>
                            <Trans>Next unread item bookmarklet</Trans>
                            <span> </span>
                            <Box ml="xl">
                                <NextUnreadBookmarklet />
                            </Box>
                        </List.Item>
                    </List>
                </Section>
                <Section title={<Trans>Keyboard shortcuts</Trans>} icon={<TbKeyboard size={24} />}>
                    <KeyboardShortcutsHelp />
                </Section>
                <Section title={<Trans>REST API</Trans>} icon={<TbRocket size={24} />}>
                    <Anchor onClick={async () => await dispatch(redirectToApiDocumentation())}>
                        <Trans>Go to the API documentation.</Trans>
                    </Anchor>
                </Section>
            </SimpleGrid>
        </Container>
    )
}
