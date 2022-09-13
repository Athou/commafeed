import { Anchor, Box, createStyles, Divider, Paper } from "@mantine/core"
import { Constants } from "app/constants"
import { markEntry, selectEntry } from "app/slices/entries"
import { useAppDispatch, useAppSelector } from "app/store"
import { Entry } from "app/types"
import React from "react"
import { FeedEntryBody } from "./FeedEntryBody"
import { FeedEntryCompactHeader } from "./FeedEntryCompactHeader"
import { FeedEntryFooter } from "./FeedEntryFooter"
import { FeedEntryHeader } from "./FeedEntryHeader"

interface FeedEntryProps {
    entry: Entry
    expanded: boolean
}

const useStyles = createStyles((theme, props: FeedEntryProps) => {
    let backgroundColor
    if (theme.colorScheme === "dark") backgroundColor = props.entry.read ? "inherit" : theme.colors.dark[5]
    else backgroundColor = props.entry.read && !props.expanded ? theme.colors.gray[0] : "inherit"

    return {
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
})

export function FeedEntry(props: FeedEntryProps) {
    const { classes } = useStyles(props)
    const viewMode = useAppSelector(state => state.user.settings?.viewMode)
    const dispatch = useAppDispatch()
    const compactHeader = viewMode === "title" && !props.expanded

    const headerClicked = (e: React.MouseEvent) => {
        if (e.button === 1 || e.ctrlKey || e.metaKey) {
            // middle click
            dispatch(markEntry({ entry: props.entry, read: true }))
        } else if (e.button === 0) {
            // main click
            // don't trigger the link
            e.preventDefault()

            dispatch(selectEntry(props.entry))
        }
    }

    return (
        <Paper shadow="xs" withBorder className={classes.paper}>
            <Anchor
                variant="text"
                href={props.entry.url}
                target="_blank"
                rel="noreferrer"
                onClick={headerClicked}
                onAuxClick={headerClicked}
            >
                <Box p="xs">
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
