import { Trans } from "@lingui/react/macro"
import { Menu } from "@mantine/core"
import { type ReactElement, useEffect, useState } from "react"
import { TbArrowBarToDown, TbExternalLink, TbMail, TbMailOpened, TbRss, TbStar, TbStarOff } from "react-icons/tb"
import { throttle } from "throttle-debounce"
import { markEntriesUpToEntry, markEntry, starEntry } from "@/app/entries/thunks"
import { redirectToFeed } from "@/app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { Entry } from "@/app/types"
import { truncate } from "@/app/utils"
import { useBrowserExtension } from "@/hooks/useBrowserExtension"

interface FeedEntryContextMenuProps {
    entry: Entry
    // element that opens the menu when right-clicked
    children: ReactElement
}

const iconSize = 16

export function FeedEntryContextMenu(props: Readonly<FeedEntryContextMenuProps>) {
    const [opened, setOpened] = useState(false)
    const customContextMenu = useAppSelector(state => state.user.settings?.customContextMenu)
    const sourceType = useAppSelector(state => state.entries.source.type)
    const dispatch = useAppDispatch()
    const { openLinkInBackgroundTab } = useBrowserExtension()

    // close context menu on scroll
    useEffect(() => {
        if (!opened) return

        const listener = throttle(100, () => setOpened(false))
        window.addEventListener("scroll", listener)
        return () => window.removeEventListener("scroll", listener)
    }, [opened])

    return (
        <Menu position="bottom-start" opened={opened} onChange={setOpened}>
            <Menu.ContextMenu disabled={!customContextMenu}>{props.children}</Menu.ContextMenu>
            <Menu.Dropdown>
                <Menu.Item
                    leftSection={<TbExternalLink size={iconSize} />}
                    onClick={() => {
                        window.open(props.entry.url, "_blank", "noreferrer")
                        dispatch(markEntry({ entry: props.entry, read: true }))
                    }}
                >
                    <Trans>Open link in new tab</Trans>
                </Menu.Item>
                <Menu.Item
                    leftSection={<TbExternalLink size={iconSize} />}
                    onClick={() => {
                        openLinkInBackgroundTab(props.entry.url)
                        dispatch(markEntry({ entry: props.entry, read: true }))
                    }}
                >
                    <Trans>Open link in new background tab</Trans>
                </Menu.Item>

                <Menu.Divider />

                {props.entry.markable && (
                    <Menu.Item
                        leftSection={props.entry.starred ? <TbStarOff size={iconSize} /> : <TbStar size={iconSize} />}
                        onClick={async () =>
                            await dispatch(starEntry({ entry: props.entry, starred: !props.entry.starred }))
                        }
                    >
                        {props.entry.starred ? <Trans>Unstar</Trans> : <Trans>Star</Trans>}
                    </Menu.Item>
                )}
                {props.entry.markable && (
                    <Menu.Item
                        leftSection={props.entry.read ? <TbMail size={iconSize} /> : <TbMailOpened size={iconSize} />}
                        onClick={async () => await dispatch(markEntry({ entry: props.entry, read: !props.entry.read }))}
                    >
                        {props.entry.read ? <Trans>Keep unread</Trans> : <Trans>Mark as read</Trans>}
                    </Menu.Item>
                )}
                <Menu.Item
                    leftSection={<TbArrowBarToDown size={iconSize} />}
                    onClick={async () => await dispatch(markEntriesUpToEntry(props.entry))}
                >
                    <Trans>Mark as read up to here</Trans>
                </Menu.Item>

                {sourceType === "category" && <Menu.Divider />}
                {sourceType === "category" && (
                    <Menu.Item
                        leftSection={<TbRss size={iconSize} />}
                        onClick={() => {
                            dispatch(redirectToFeed(props.entry.feedId))
                        }}
                    >
                        <Trans>Go to {truncate(props.entry.feedName, 30)}</Trans>
                    </Menu.Item>
                )}
            </Menu.Dropdown>
        </Menu>
    )
}
