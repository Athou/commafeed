import { Highlight } from "@mantine/core"
import { useAppSelector } from "@/app/store"
import type { Entry } from "@/app/types"
import { tss } from "@/tss"

export interface FeedEntryTitleProps {
    entry: Entry
}

const useStyles = tss.create(() => ({
    content: {
        textWrap: "inherit",
    },
}))

export function FeedEntryTitle(props: Readonly<FeedEntryTitleProps>) {
    const search = useAppSelector(state => state.entries.search)
    const keywords = search?.split(" ")

    const { classes } = useStyles()
    return (
        <Highlight
            inherit
            highlight={keywords ?? ""}
            // make sure ellipsis is shown when title is too long
            span
            className={classes.content}
        >
            {props.entry.title}
        </Highlight>
    )
}
