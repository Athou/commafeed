import { Trans } from "@lingui/react/macro"
import { Group } from "@mantine/core"
import { Item, Menu, Separator } from "react-contexify"
import { TbArrowBarToDown, TbExternalLink, TbMail, TbMailOpened, TbRss, TbStar, TbStarOff } from "react-icons/tb"
import { Constants } from "@/app/constants"
import { markEntriesUpToEntry, markEntry, starEntry } from "@/app/entries/thunks"
import { redirectToFeed } from "@/app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { Entry } from "@/app/types"
import { truncate } from "@/app/utils"
import { useBrowserExtension } from "@/hooks/useBrowserExtension"
import { useColorScheme } from "@/hooks/useColorScheme"
import { tss } from "@/tss"

interface FeedEntryContextMenuProps {
    entry: Entry
}

const iconSize = 16
const useStyles = tss.create(({ theme, colorScheme }) => ({
    menu: {
        // apply mantine theme from MenuItem.styles.ts
        fontSize: theme.fontSizes.sm,
        "--contexify-item-color": `${colorScheme === "dark" ? theme.colors.dark[0] : theme.black} !important`,
        "--contexify-activeItem-color": `${colorScheme === "dark" ? theme.colors.dark[0] : theme.black} !important`,
        "--contexify-activeItem-bgColor": `${colorScheme === "dark" ? theme.colors.dark[4] : theme.colors.gray[1]} !important`,
    },
}))

export function FeedEntryContextMenu(props: FeedEntryContextMenuProps) {
    const colorScheme = useColorScheme()
    const { classes } = useStyles()
    const sourceType = useAppSelector(state => state.entries.source.type)
    const dispatch = useAppDispatch()
    const { openLinkInBackgroundTab } = useBrowserExtension()

    return (
        <Menu id={Constants.dom.entryContextMenuId(props.entry)} theme={colorScheme} animation={false} className={classes.menu}>
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

            <Item onClick={async () => await dispatch(starEntry({ entry: props.entry, starred: !props.entry.starred }))}>
                <Group>
                    {props.entry.starred ? <TbStarOff size={iconSize} /> : <TbStar size={iconSize} />}
                    {props.entry.starred ? <Trans>Unstar</Trans> : <Trans>Star</Trans>}
                </Group>
            </Item>
            {props.entry.markable && (
                <Item onClick={async () => await dispatch(markEntry({ entry: props.entry, read: !props.entry.read }))}>
                    <Group>
                        {props.entry.read ? <TbMail size={iconSize} /> : <TbMailOpened size={iconSize} />}
                        {props.entry.read ? <Trans>Keep unread</Trans> : <Trans>Mark as read</Trans>}
                    </Group>
                </Item>
            )}
            <Item onClick={async () => await dispatch(markEntriesUpToEntry(props.entry))}>
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
