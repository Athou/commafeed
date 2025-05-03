import { Box, Flex, Space } from "@mantine/core"
import type { Entry } from "app/types"
import { RelativeDate } from "components/RelativeDate"
import { FeedFavicon } from "components/content/FeedFavicon"
import { OpenExternalLink } from "components/content/header/OpenExternalLink"
import { Star } from "components/content/header/Star"
import { tss } from "tss"
import { FeedEntryTitle } from "./FeedEntryTitle"

export interface FeedEntryHeaderProps {
    entry: Entry
    expanded: boolean
    showStarIcon?: boolean
    showExternalLinkIcon?: boolean
}

const useStyles = tss
    .withParams<{
        read: boolean
    }>()
    .create(({ colorScheme, read }) => ({
        main: {
            fontWeight: colorScheme === "light" && !read ? "bold" : "inherit",
        },
    }))

export function FeedEntryHeader(props: FeedEntryHeaderProps) {
    const { classes } = useStyles({
        read: props.entry.read,
    })
    return (
        <Box>
            <Flex align="flex-start" justify="space-between">
                <Flex align="flex-start" className={classes.main}>
                    {props.showStarIcon && (
                        <Box ml={-5}>
                            <Star entry={props.entry} />
                        </Box>
                    )}
                    <FeedEntryTitle entry={props.entry} />
                </Flex>
                {props.showExternalLinkIcon && <OpenExternalLink entry={props.entry} />}
            </Flex>
            <Flex align="center">
                <FeedFavicon url={props.entry.iconUrl} />
                <Space w={6} />
                <Box c="dimmed">
                    {props.entry.feedName}
                    <span> · </span>
                    <RelativeDate date={props.entry.date} />
                </Box>
            </Flex>
            {props.expanded && (
                <Box c="dimmed">
                    {props.entry.author && <span>by {props.entry.author}</span>}
                    {props.entry.author && props.entry.categories && <span>&nbsp;·&nbsp;</span>}
                    {props.entry.categories && <span>{props.entry.categories}</span>}
                </Box>
            )}
        </Box>
    )
}
