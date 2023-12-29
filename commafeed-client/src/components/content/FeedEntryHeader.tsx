import { Box, type MantineTheme, Space, Text, useMantineTheme } from "@mantine/core"
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
        theme: MantineTheme
        read: boolean
    }>()
    .create(({ theme, read }) => ({
        headerText: {
            fontWeight: theme.colorScheme === "light" && !read ? "bold" : "inherit",
        },
        headerSubtext: {
            display: "flex",
            alignItems: "center",
            fontSize: "90%",
        },
    }))

export function FeedEntryHeader(props: FeedEntryHeaderProps) {
    const theme = useMantineTheme()
    const { classes } = useStyles({
        theme,
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
                <Text color="dimmed">
                    {props.entry.feedName}
                    <span> · </span>
                    <RelativeDate date={props.entry.date} />
                </Text>
            </Box>
            {props.expanded && (
                <Box className={classes.headerSubtext}>
                    <Text color="dimmed">
                        {props.entry.author && <span>by {props.entry.author}</span>}
                        {props.entry.author && props.entry.categories && <span>&nbsp;·&nbsp;</span>}
                        {props.entry.categories && <span>{props.entry.categories}</span>}
                    </Text>
                </Box>
            )}
        </Box>
    )
}
