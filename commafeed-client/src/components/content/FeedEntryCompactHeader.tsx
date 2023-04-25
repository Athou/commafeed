import { Box, createStyles, Text } from "@mantine/core"
import { Entry } from "app/types"
import { RelativeDate } from "components/RelativeDate"
import { OnDesktop } from "components/responsive/OnDesktop"
import { FeedEntryTitle } from "./FeedEntryTitle"
import { FeedFavicon } from "./FeedFavicon"

export interface FeedEntryHeaderProps {
    entry: Entry
}

const useStyles = createStyles((theme, props: FeedEntryHeaderProps) => ({
    wrapper: {
        display: "flex",
        alignItems: "center",
        columnGap: "10px",
    },
    title: {
        flexGrow: 1,
        fontWeight: theme.colorScheme === "light" && !props.entry.read ? "bold" : "inherit",
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    feedName: {
        width: "145px",
        minWidth: "145px",
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    date: {
        whiteSpace: "nowrap",
    },
}))
export function FeedEntryCompactHeader(props: FeedEntryHeaderProps) {
    const { classes } = useStyles(props)
    return (
        <Box className={classes.wrapper}>
            <Box>
                <FeedFavicon url={props.entry.iconUrl} />
            </Box>
            <OnDesktop>
                <Text color="dimmed" className={classes.feedName}>
                    {props.entry.feedName}
                </Text>
            </OnDesktop>
            <Box className={classes.title}>
                <FeedEntryTitle entry={props.entry} />
            </Box>
            <OnDesktop>
                <Text color="dimmed" className={classes.date}>
                    <RelativeDate date={props.entry.date} />
                </Text>
            </OnDesktop>
        </Box>
    )
}
