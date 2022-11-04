import { Anchor, Box, createStyles, Divider, Paper } from "@mantine/core"
import { Constants } from "app/constants"
import { markEntry } from "app/slices/entries"
import { useAppDispatch, useAppSelector } from "app/store"
import { Entry } from "app/types"
import React from "react"
import { useSwipeable } from "react-swipeable"
import { FeedEntryBody } from "./FeedEntryBody"
import { FeedEntryCompactHeader } from "./FeedEntryCompactHeader"
import { FeedEntryFooter } from "./FeedEntryFooter"
import { FeedEntryHeader } from "./FeedEntryHeader"

interface FeedEntryProps {
    entry: Entry
    expanded: boolean
    showSelectionIndicator: boolean
    onHeaderClick: (e: React.MouseEvent) => void
}

const useStyles = createStyles((theme, props: FeedEntryProps) => {
    let backgroundColor
    if (theme.colorScheme === "dark") backgroundColor = props.entry.read ? "inherit" : theme.colors.dark[5]
    else backgroundColor = props.entry.read && !props.expanded ? theme.colors.gray[0] : "inherit"

    const styles = {
        paper: {
            backgroundColor,
            marginTop: theme.spacing.xs,
            marginBottom: theme.spacing.xs,
            [theme.fn.smallerThan(Constants.layout.mobileBreakpoint)]: {
                marginTop: "6px",
                marginBottom: "6px",
            },
        },
        body: {
            maxWidth: Constants.layout.entryMaxWidth,
        },
    }

    if (props.showSelectionIndicator) {
        styles.paper.borderLeftColor = theme.colorScheme === "dark" ? theme.colors.orange[4] : theme.colors.orange[6]
    }

    return styles
})

export function FeedEntry(props: FeedEntryProps) {
    const { classes } = useStyles(props)
    const viewMode = useAppSelector(state => state.user.settings?.viewMode)
    const dispatch = useAppDispatch()

    const compactHeader = viewMode === "title" && !props.expanded

    const swipeHandlers = useSwipeable({
        onSwipedRight: () => dispatch(markEntry({ entry: props.entry, read: !props.entry.read })),
    })

    return (
        <Paper withBorder className={classes.paper}>
            <Anchor
                variant="text"
                href={props.entry.url}
                target="_blank"
                rel="noreferrer"
                onClick={props.onHeaderClick}
                onAuxClick={props.onHeaderClick}
            >
                <Box p="xs" {...swipeHandlers}>
                    {compactHeader && <FeedEntryCompactHeader entry={props.entry} />}
                    {!compactHeader && <FeedEntryHeader entry={props.entry} expanded={props.expanded} />}
                </Box>
            </Anchor>
            {props.expanded && (
                <Box px="xs" pb="xs">
                    <Box className={classes.body} sx={{ direction: props.entry.rtl ? "rtl" : "ltr" }}>
                        <FeedEntryBody entry={props.entry} />
                    </Box>
                    <Divider variant="dashed" my="xs" />
                    <FeedEntryFooter entry={props.entry} />
                </Box>
            )}
        </Paper>
    )
}
