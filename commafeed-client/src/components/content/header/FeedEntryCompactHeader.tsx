import { Box } from "@mantine/core"
import type { Entry } from "@/app/types"
import { FeedFavicon } from "@/components/content/FeedFavicon"
import { OpenExternalLink } from "@/components/content/header/OpenExternalLink"
import { Star } from "@/components/content/header/Star"
import { RelativeDate } from "@/components/RelativeDate"
import { OnDesktop } from "@/components/responsive/OnDesktop"
import { tss } from "@/tss"
import { FeedEntryTitle } from "./FeedEntryTitle"

export interface FeedEntryHeaderProps {
    entry: Entry
    showStarIcon?: boolean
    showExternalLinkIcon?: boolean
}

const useStyles = tss
    .withParams<{
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
    const { classes } = useStyles({
        read: props.entry.read,
    })
    return (
        <Box className={classes.wrapper}>
            {props.showStarIcon && <Star entry={props.entry} />}
            <Box>
                <FeedFavicon url={props.entry.iconUrl} />
            </Box>
            <OnDesktop>
                <Box c="dimmed" className={classes.feedName}>
                    {props.entry.feedName}
                </Box>
            </OnDesktop>
            <Box className={classes.title}>
                <FeedEntryTitle entry={props.entry} />
            </Box>
            <OnDesktop>
                <Box c="dimmed" className={classes.date}>
                    <RelativeDate date={props.entry.date} />
                </Box>
            </OnDesktop>
            {props.showExternalLinkIcon && <OpenExternalLink entry={props.entry} />}
        </Box>
    )
}
