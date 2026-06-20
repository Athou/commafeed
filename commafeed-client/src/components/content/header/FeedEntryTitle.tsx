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
    highlighted: {
        backgroundColor: "var(--mantine-color-yellow-light)",
        borderRadius: "var(--mantine-radius-xs)",
        boxDecorationBreak: "clone",
        fontWeight: 600,
        paddingInline: 4,
    },
}))

export function FeedEntryTitle(props: Readonly<FeedEntryTitleProps>) {
    const search = useAppSelector(state => state.entries.search)
    const keywords = search?.split(" ")

    const { classes, cx } = useStyles()
    return (
        <Highlight
            inherit
            highlight={keywords ?? ""}
            // make sure ellipsis is shown when title is too long
            span
            className={cx(classes.content, { [classes.highlighted]: props.entry.highlighted })}
        >
            {props.entry.title}
        </Highlight>
    )
}
