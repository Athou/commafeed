import { Box, Flex, Space, Text } from "@mantine/core"
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
        details: {
            fontSize: "90%",
        },
    }))

export function FeedEntryHeader(props: FeedEntryHeaderProps) {
    const { classes } = useStyles({
        read: props.entry.read,
    })
    return (
        <Box className="cf-FeedEntryHeader">
            <Flex align="flex-start" justify="space-between" className="cf-FeedEntryHeader-Flex1">
                <Flex align="flex-start" className={`${classes.main} cf-FeedEntryHeader-Flex1-Flex`}>
                    {props.showStarIcon && (
                        <Box ml={-5} className="cf-FeedEntryHeader-Flex1-Flex-Box">
                            <Star entry={props.entry} />
                        </Box>
                    )}
                    <FeedEntryTitle entry={props.entry} />
                </Flex>
                {props.showExternalLinkIcon && <OpenExternalLink entry={props.entry} />}
            </Flex>
            <Flex align="center" className={`${classes.details} cf-FeedEntryHeader-Flex2`}>
                <FeedFavicon url={props.entry.iconUrl} />
                <Space w={6} />
                <Text c="dimmed" className="cf-FeedEntryHeader-Flex2-Text">
                    {props.entry.feedName}
                    <span> · </span>
                    <RelativeDate date={props.entry.date} />
                </Text>
            </Flex>
            {props.expanded && (
                <Box className={`${classes.details} cf-FeedEntryHeader-Box`}>
                    <Text c="dimmed" className="cf-FeedEntryHeader-Box-Text">
                        {props.entry.author && <span>by {props.entry.author}</span>}
                        {props.entry.author && props.entry.categories && <span>&nbsp;·&nbsp;</span>}
                        {props.entry.categories && <span>{props.entry.categories}</span>}
                    </Text>
                </Box>
            )}
        </Box>
    )
}
