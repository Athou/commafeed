import { Trans } from "@lingui/macro"
import { createStyles, Group } from "@mantine/core"
import { markEntriesUpToEntry, markEntry, starEntry } from "app/slices/entries"
import { redirectToFeed } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { Entry } from "app/types"
import { truncate } from "app/utils"
import { useBrowserExtension } from "hooks/useBrowserExtension"
import { useEffect } from "react"
import { Item, Menu, Separator, useContextMenu } from "react-contexify"
import { TbArrowBarToDown, TbExternalLink, TbEyeCheck, TbEyeOff, TbRss, TbStar, TbStarOff } from "react-icons/tb"
import { throttle } from "throttle-debounce"

interface FeedEntryContextMenuProps {
    entry: Entry
}

const iconSize = 16
const useStyles = createStyles(theme => ({
    menu: {
        // apply mantine theme from MenuItem.styles.ts
        fontSize: theme.fontSizes.sm,
        "--contexify-item-color": `${theme.colorScheme === "dark" ? theme.colors.dark[0] : theme.black} !important`,
        "--contexify-activeItem-color": `${theme.colorScheme === "dark" ? theme.colors.dark[0] : theme.black} !important`,
        "--contexify-activeItem-bgColor": `${
            theme.colorScheme === "dark" ? theme.fn.rgba(theme.colors.dark[3], 0.35) : theme.colors.gray[1]
        } !important`,
    },
}))

const menuId = (entry: Entry) => entry.id

export function FeedEntryContextMenu(props: FeedEntryContextMenuProps) {
    const { classes, theme } = useStyles()
    const sourceType = useAppSelector(state => state.entries.source.type)
    const dispatch = useAppDispatch()
    const { openLinkInBackgroundTab } = useBrowserExtension()

    return (
        <Menu id={menuId(props.entry)} theme={theme.colorScheme} animation={false} className={classes.menu}>
            <Item
                onClick={() => {
                    window.open(props.entry.url, "_blank", "noreferrer")
                    dispatch(markEntry({ entry: props.entry, read: true }))
                }}
            >
                <Group>
                    <TbExternalLink size={iconSize} />
                    <Trans>Open link in new tab</Trans>
                </Group>
            </Item>
            <Item
                onClick={() => {
                    openLinkInBackgroundTab(props.entry.url)
                    dispatch(markEntry({ entry: props.entry, read: true }))
                }}
            >
                <Group>
                    <TbExternalLink size={iconSize} />
                    <Trans>Open link in new background tab</Trans>
                </Group>
            </Item>

            <Separator />

            <Item onClick={() => dispatch(starEntry({ entry: props.entry, starred: !props.entry.starred }))}>
                <Group>
                    {props.entry.starred ? <TbStarOff size={iconSize} /> : <TbStar size={iconSize} />}
                    {props.entry.starred ? <Trans>Unstar</Trans> : <Trans>Star</Trans>}
                </Group>
            </Item>
            <Item onClick={() => dispatch(markEntry({ entry: props.entry, read: !props.entry.read }))}>
                <Group>
                    {props.entry.read ? <TbEyeOff size={iconSize} /> : <TbEyeCheck size={iconSize} />}
                    {props.entry.read ? <Trans>Keep unread</Trans> : <Trans>Mark as read</Trans>}
                </Group>
            </Item>
            <Item onClick={() => dispatch(markEntriesUpToEntry(props.entry))}>
                <Group>
                    <TbArrowBarToDown size={iconSize} />
                    <Trans>Mark as read up to here</Trans>
                </Group>
            </Item>

            {sourceType === "category" && (
                <>
                    <Separator />

                    <Item
                        onClick={() => {
                            dispatch(redirectToFeed(props.entry.feedId))
                        }}
                    >
                        <Group>
                            <TbRss size={iconSize} />
                            <Trans>Go to {truncate(props.entry.feedName, 30)}</Trans>
                        </Group>
                    </Item>
                </>
            )}
        </Menu>
    )
}

export function useFeedEntryContextMenu(entry: Entry) {
    const contextMenu = useContextMenu({
        id: menuId(entry),
    })

    const onContextMenu = (event: React.MouseEvent) => {
        event.preventDefault()
        contextMenu.show({
            event,
        })
    }

    // close context menu on scroll
    useEffect(() => {
        const listener = () => contextMenu.hideAll()
        const throttledListener = throttle(100, listener)

        window.addEventListener("scroll", throttledListener)
        return () => window.removeEventListener("scroll", throttledListener)
    }, [contextMenu])

    return { onContextMenu }
}
