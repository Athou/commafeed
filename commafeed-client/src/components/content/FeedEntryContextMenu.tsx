import { t, Trans } from "@lingui/macro"
import { createStyles, Group } from "@mantine/core"
import { Constants } from "app/constants"
import { markEntriesUpToEntry, markEntry, starEntry } from "app/slices/entries"
import { useAppDispatch } from "app/store"
import { Entry } from "app/types"
import { openLinkInBackgroundTab } from "app/utils"
import { throttle } from "lodash"
import { useEffect } from "react"
import { Item, Menu, Separator, useContextMenu } from "react-contexify"
import { TbArrowBarToDown, TbExternalLink, TbEyeCheck, TbEyeOff, TbStar, TbStarOff } from "react-icons/tb"

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
    const dispatch = useAppDispatch()

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

            <Item onClick={() => dispatch(markEntry({ entry: props.entry, read: !props.entry.read }))}>
                <Group>
                    {props.entry.read ? <TbEyeOff size={iconSize} /> : <TbEyeCheck size={iconSize} />}
                    {props.entry.read ? t`Keep unread` : t`Mark as read`}
                </Group>
            </Item>
            <Item onClick={() => dispatch(starEntry({ entry: props.entry, starred: !props.entry.starred }))}>
                <Group>
                    {props.entry.starred ? <TbStarOff size={iconSize} /> : <TbStar size={iconSize} />}
                    {props.entry.starred ? t`Unstar` : t`Star`}
                </Group>
            </Item>

            <Separator />

            <Item onClick={() => dispatch(markEntriesUpToEntry(props.entry))}>
                <Group>
                    <TbArrowBarToDown size={iconSize} />
                    <Trans>Mark as read up to here</Trans>
                </Group>
            </Item>
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
        const scrollArea = document.getElementById(Constants.dom.mainScrollAreaId)

        const listener = () => contextMenu.hideAll()
        const throttledListener = throttle(listener, 100)

        scrollArea?.addEventListener("scroll", throttledListener)
        return () => scrollArea?.removeEventListener("scroll", throttledListener)
    }, [contextMenu])

    return { onContextMenu }
}
