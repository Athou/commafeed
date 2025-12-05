import { Box, Divider, type MantineRadius, type MantineSpacing, Paper } from "@mantine/core"
import type React from "react"
import { useSwipeable } from "react-swipeable"
import { Constants } from "@/app/constants"
import { useAppSelector } from "@/app/store"
import type { Entry, ViewMode } from "@/app/types"
import { FeedEntryCompactHeader } from "@/components/content/header/FeedEntryCompactHeader"
import { FeedEntryHeader } from "@/components/content/header/FeedEntryHeader"
import { useMobile } from "@/hooks/useMobile"
import { tss } from "@/tss"
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
        fontSizePercentage: number
    }>()
    .create(({ theme, colorScheme, read, expanded, viewMode, rtl, showSelectionIndicator, maxWidth, fontSizePercentage }) => {
        let backgroundColor: string
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

        let paperBorderLeftColor = ""
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
                fontSize: `${fontSizePercentage}%`,
                color: "inherit",
                textDecoration: "none",
            },
            body: {
                fontSize: `${fontSizePercentage}%`,
                direction: rtl ? "rtl" : "ltr",
                maxWidth: maxWidth ?? "100%",
            },
        }
    })

export function FeedEntry(props: Readonly<FeedEntryProps>) {
    const viewMode = useAppSelector(state => state.user.localSettings.viewMode)
    const fontSizePercentage = useAppSelector(state => state.user.localSettings.fontSizePercentage)
    const { classes, cx } = useStyles({
        read: props.entry.read,
        expanded: props.expanded,
        viewMode,
        rtl: props.entry.rtl,
        showSelectionIndicator: props.showSelectionIndicator,
        maxWidth: props.maxWidth,
        fontSizePercentage,
    })

    const externalLinkDisplayMode = useAppSelector(state => state.user.settings?.externalLinkIconDisplayMode)
    const starIconDisplayMode = useAppSelector(state => state.user.settings?.starIconDisplayMode)
    const mobile = useMobile()

    const showExternalLinkIcon =
        externalLinkDisplayMode && ["always", mobile ? "on_mobile" : "on_desktop"].includes(externalLinkDisplayMode)
    const showStarIcon =
        props.entry.markable && starIconDisplayMode && ["always", mobile ? "on_mobile" : "on_desktop"].includes(starIconDisplayMode)

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
            component="article"
            id={Constants.dom.entryId(props.entry)}
            data-id={props.entry.id}
            data-feed-id={props.entry.feedId}
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
                    {compactHeader && (
                        <FeedEntryCompactHeader
                            entry={props.entry}
                            showStarIcon={showStarIcon}
                            showExternalLinkIcon={showExternalLinkIcon}
                        />
                    )}
                    {!compactHeader && (
                        <FeedEntryHeader
                            entry={props.entry}
                            expanded={props.expanded}
                            showStarIcon={showStarIcon}
                            showExternalLinkIcon={showExternalLinkIcon}
                        />
                    )}
                </Box>
            </a>
            {props.expanded && (
                <Box px={paddingX} pb={paddingY} onClick={props.onBodyClick}>
                    <Box className={`${classes.body} cf-content`}>
                        <FeedEntryBody entry={props.entry} />
                    </Box>
                    <Divider variant="dashed" my={paddingY} className="cf-footer-divider" />
                    <FeedEntryFooter entry={props.entry} />
                </Box>
            )}

            <FeedEntryContextMenu entry={props.entry} />
        </Paper>
    )
}
