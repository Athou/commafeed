import { Box, createStyles, Text } from "@mantine/core"
import { Entry } from "app/types"
import { RelativeDate } from "components/RelativeDate"
import { FeedEntryTitle } from "./FeedEntryTitle"
import { FeedFavicon } from "./FeedFavicon"

export interface FeedEntryHeaderProps {
    entry: Entry
    expanded: boolean
}

const useStyles = createStyles((theme, props: FeedEntryHeaderProps) => ({
    headerText: {
        fontWeight: theme.colorScheme === "light" && !props.entry.read ? "bold" : "inherit",
        whiteSpace: props.expanded ? "inherit" : "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    headerSubtext: {
        display: "flex",
        alignItems: "center",
        fontSize: "90%",
        whiteSpace: props.expanded ? "inherit" : "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
}))
export function FeedEntryHeader(props: FeedEntryHeaderProps) {
    const { classes } = useStyles(props)
    return (
        <Box>
            <Box className={classes.headerText}>
                <FeedEntryTitle entry={props.entry} />
            </Box>
            <Box className={classes.headerSubtext}>
                <Box mr={6}>
                    <FeedFavicon url={props.entry.iconUrl} />
                </Box>
                <Box>
                    <Text color="dimmed">{props.entry.feedName}</Text>
                </Box>
                <Box>
                    <Text color="dimmed">
                        <span>&nbsp;·&nbsp;</span>
                        <RelativeDate date={props.entry.date} />
                    </Text>
                </Box>
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
