import { Box, Divider, type MantineRadius, type MantineSpacing, Paper } from "@mantine/core"
import { Constants } from "app/constants"
import { type Entry, type ViewMode } from "app/types"
import { FeedEntryCompactHeader } from "components/content/header/FeedEntryCompactHeader"
import { FeedEntryHeader } from "components/content/header/FeedEntryHeader"
import { useViewMode } from "hooks/useViewMode"
import React from "react"
import { useSwipeable } from "react-swipeable"
import { tss } from "tss"
import { FeedEntryBody } from "./FeedEntryBody"
import { FeedEntryContextMenu } from "./FeedEntryContextMenu"
import { FeedEntryFooter } from "./FeedEntryFooter"

interface FeedEntryProps {
    entry: Entry
    expanded: boolean
    selected: boolean
    showSelectionIndicator: boolean
    maxWidth?: number
    onHeaderClick: (e: React.MouseEvent) => void
    onHeaderRightClick: (e: React.MouseEvent) => void
    onBodyClick: (e: React.MouseEvent) => void
    onSwipedLeft: () => void
}

const useStyles = tss
    .withParams<{
        read: boolean
        expanded: boolean
        viewMode: ViewMode
        rtl: boolean
        showSelectionIndicator: boolean
        maxWidth?: number
    }>()
    .create(({ theme, colorScheme, read, expanded, viewMode, rtl, showSelectionIndicator, maxWidth }) => {
        let backgroundColor
        if (colorScheme === "dark") {
            backgroundColor = read ? "inherit" : theme.colors.dark[5]
        } else {
            backgroundColor = read && !expanded ? theme.colors.gray[0] : "inherit"
        }

        let marginY = 10
        if (viewMode === "title") {
            marginY = 2
        } else if (viewMode === "cozy") {
            marginY = 6
        }

        let mobileMarginY = 6
        if (viewMode === "title") {
            mobileMarginY = 2
        } else if (viewMode === "cozy") {
            mobileMarginY = 4
        }

        let backgroundHoverColor = backgroundColor
        if (!expanded && !read) {
            backgroundHoverColor = colorScheme === "dark" ? theme.colors.dark[6] : theme.colors.gray[1]
        }

        let paperBorderLeftColor
        if (showSelectionIndicator) {
            const borderLeftColor = colorScheme === "dark" ? theme.colors[theme.primaryColor][4] : theme.colors[theme.primaryColor][6]
            paperBorderLeftColor = `${borderLeftColor} !important`
        }

        return {
            paper: {
                backgroundColor,
                borderLeftColor: paperBorderLeftColor,
                marginTop: marginY,
                marginBottom: marginY,
                [`@media (max-width: ${Constants.layout.mobileBreakpoint}px)`]: {
                    marginTop: mobileMarginY,
                    marginBottom: mobileMarginY,
                },
                "@media (hover: hover)": {
                    "&:hover": {
                        backgroundColor: backgroundHoverColor,
                    },
                },
            },
            headerLink: {
                color: "inherit",
                textDecoration: "none",
            },
            body: {
                direction: rtl ? "rtl" : "ltr",
                maxWidth: maxWidth ?? "100%",
            },
        }
    })

export function FeedEntry(props: FeedEntryProps) {
    const { viewMode } = useViewMode()
    const { classes, cx } = useStyles({
        read: props.entry.read,
        expanded: props.expanded,
        viewMode,
        rtl: props.entry.rtl,
        showSelectionIndicator: props.showSelectionIndicator,
        maxWidth: props.maxWidth,
    })

    const swipeHandlers = useSwipeable({
        onSwipedLeft: props.onSwipedLeft,
    })

    let paddingX: MantineSpacing = "xs"
    if (viewMode === "title" || viewMode === "cozy") paddingX = 6

    let paddingY: MantineSpacing = "xs"
    if (viewMode === "title") {
        paddingY = 4
    } else if (viewMode === "cozy") {
        paddingY = 8
    }

    let borderRadius: MantineRadius = "sm"
    if (viewMode === "title") {
        borderRadius = 0
    } else if (viewMode === "cozy") {
        borderRadius = "xs"
    }

    const compactHeader = !props.expanded && (viewMode === "title" || viewMode === "cozy")
    return (
        <Paper
            withBorder
            radius={borderRadius}
            className={cx(classes.paper, {
                read: props.entry.read,
                unread: !props.entry.read,
                expanded: props.expanded,
                selected: props.selected,
                "show-selection-indicator": props.showSelectionIndicator,
            })}
        >
            <a
                className={classes.headerLink}
                href={props.entry.url}
                target="_blank"
                rel="noreferrer"
                onClick={props.onHeaderClick}
                onAuxClick={props.onHeaderClick}
                onContextMenu={props.onHeaderRightClick}
            >
                <Box px={paddingX} py={paddingY} {...swipeHandlers}>
                    {compactHeader && <FeedEntryCompactHeader entry={props.entry} />}
                    {!compactHeader && <FeedEntryHeader entry={props.entry} expanded={props.expanded} />}
                </Box>
            </a>
            {props.expanded && (
                <Box px={paddingX} pb={paddingY} onClick={props.onBodyClick}>
                    <Box className={classes.body}>
                        <FeedEntryBody entry={props.entry} />
                    </Box>
                    <Divider variant="dashed" my={paddingY} />
                    <FeedEntryFooter entry={props.entry} />
                </Box>
            )}

            <FeedEntryContextMenu entry={props.entry} />
        </Paper>
    )
}
