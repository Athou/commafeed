import { Box, Text } from "@mantine/core"
import { type Entry } from "app/types"
import { RelativeDate } from "components/RelativeDate"
import { OnDesktop } from "components/responsive/OnDesktop"
import { useColorScheme } from "hooks/useColorScheme"
import { tss } from "tss"
import { FeedEntryTitle } from "./FeedEntryTitle"
import { FeedFavicon } from "./FeedFavicon"

export interface FeedEntryHeaderProps {
    entry: Entry
}

const useStyles = tss
    .withParams<{
        colorScheme: "light" | "dark"
        read: boolean
    }>()
    .create(({ colorScheme, read }) => ({
        wrapper: {
            display: "flex",
            alignItems: "center",
            columnGap: "10px",
        },
        title: {
            flexGrow: 1,
            fontWeight: colorScheme === "light" && !read ? "bold" : "inherit",
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
    const colorScheme = useColorScheme()
    const { classes } = useStyles({
        colorScheme,
        read: props.entry.read,
    })
    return (
        <Box className={classes.wrapper}>
            <Box>
                <FeedFavicon url={props.entry.iconUrl} />
            </Box>
            <OnDesktop>
                <Text c="dimmed" className={classes.feedName}>
                    {props.entry.feedName}
                </Text>
            </OnDesktop>
            <Box className={classes.title}>
                <FeedEntryTitle entry={props.entry} />
            </Box>
            <OnDesktop>
                <Text c="dimmed" className={classes.date}>
                    <RelativeDate date={props.entry.date} />
                </Text>
            </OnDesktop>
        </Box>
    )
}
