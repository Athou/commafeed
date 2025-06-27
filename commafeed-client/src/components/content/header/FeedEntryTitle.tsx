import { Highlight } from "@mantine/core"
import { useAppSelector } from "@/app/store"
import type { Entry } from "@/app/types"

export interface FeedEntryTitleProps {
    entry: Entry
}

export function FeedEntryTitle(props: FeedEntryTitleProps) {
    const search = useAppSelector(state => state.entries.search)
    const keywords = search?.split(" ")
    return (
        <Highlight
            inherit
            highlight={keywords ?? ""}
            // make sure ellipsis is shown when title is too long
            span
        >
            {props.entry.title}
        </Highlight>
    )
}
