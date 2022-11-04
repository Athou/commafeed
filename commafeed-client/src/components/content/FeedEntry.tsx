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

const useStyles = createStyles((theme, props: FeedEntryProps & { compact: boolean }) => {
    let backgroundColor
    if (theme.colorScheme === "dark") backgroundColor = props.entry.read ? "inherit" : theme.colors.dark[5]
    else backgroundColor = props.entry.read && !props.expanded ? theme.colors.gray[0] : "inherit"

    let marginY = theme.spacing.xs
    if (props.compact) marginY = 6

    let mobileMarginY = 6
    if (props.compact) mobileMarginY = 4

    const styles = {
        paper: {
            backgroundColor,
            marginTop: marginY,
            marginBottom: marginY,
            [theme.fn.smallerThan(Constants.layout.mobileBreakpoint)]: {
                marginTop: mobileMarginY,
                marginBottom: mobileMarginY,
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
    const viewMode = useAppSelector(state => state.user.settings?.viewMode)
    const compact = viewMode === "title"
    const compactHeader = compact && !props.expanded

    const { classes } = useStyles({ ...props, compact })

    const dispatch = useAppDispatch()

    const swipeHandlers = useSwipeable({
        onSwipedRight: () => dispatch(markEntry({ entry: props.entry, read: !props.entry.read })),
    })

    const spacing = compact ? 8 : "xs"
    const borderRadius = compact ? "xs" : "sm"

    return (
        <Paper withBorder radius={borderRadius} className={classes.paper}>
            <Anchor
                variant="text"
                href={props.entry.url}
                target="_blank"
                rel="noreferrer"
                onClick={props.onHeaderClick}
                onAuxClick={props.onHeaderClick}
            >
                <Box p={spacing} {...swipeHandlers}>
                    {compactHeader && <FeedEntryCompactHeader entry={props.entry} />}
                    {!compactHeader && <FeedEntryHeader entry={props.entry} expanded={props.expanded} />}
                </Box>
            </Anchor>
            {props.expanded && (
                <Box px={spacing} pb={spacing}>
                    <Box className={classes.body} sx={{ direction: props.entry.rtl ? "rtl" : "ltr" }}>
                        <FeedEntryBody entry={props.entry} />
                    </Box>
                    <Divider variant="dashed" my={spacing} />
                    <FeedEntryFooter entry={props.entry} />
                </Box>
            )}
        </Paper>
    )
}
