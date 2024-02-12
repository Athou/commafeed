import { Box, Space, Text } from "@mantine/core"
import { type Entry } from "app/types"
import { RelativeDate } from "components/RelativeDate"
import { tss } from "tss"
import { FeedEntryTitle } from "./FeedEntryTitle"
import { FeedFavicon } from "./FeedFavicon"

export interface FeedEntryHeaderProps {
    entry: Entry
    expanded: boolean
}

const useStyles = tss
    .withParams<{
        read: boolean
    }>()
    .create(({ colorScheme, read }) => ({
        headerText: {
            fontWeight: colorScheme === "light" && !read ? "bold" : "inherit",
        },
        headerSubtext: {
            display: "flex",
            alignItems: "center",
            fontSize: "90%",
        },
    }))

export function FeedEntryHeader(props: FeedEntryHeaderProps) {
    const { classes } = useStyles({
        read: props.entry.read,
    })
    return (
        <Box>
            <Box className={classes.headerText}>
                <FeedEntryTitle entry={props.entry} />
            </Box>
            <Box className={classes.headerSubtext}>
                <FeedFavicon url={props.entry.iconUrl} />
                <Space w={6} />
                <Text c="dimmed">
                    {props.entry.feedName}
                    <span> · </span>
                    <RelativeDate date={props.entry.date} />
                </Text>
            </Box>
            {props.expanded && (
                <Box className={classes.headerSubtext}>
                    <Text c="dimmed">
                        {props.entry.author && <span>by {props.entry.author}</span>}
                        {props.entry.author && props.entry.categories && <span>&nbsp;·&nbsp;</span>}
                        {props.entry.categories && <span>{props.entry.categories}</span>}
                    </Text>
                </Box>
            )}
        </Box>
    )
}
