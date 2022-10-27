import { Highlight } from "@mantine/core"
import { useAppSelector } from "app/store"
import { Entry } from "app/types"

export interface FeedEntryTitleProps {
    entry: Entry
}

export function FeedEntryTitle(props: FeedEntryTitleProps) {
    const search = useAppSelector(state => state.entries.search)
    const keywords = search?.split(" ")
    return <Highlight highlight={keywords ?? ""}>{props.entry.title}</Highlight>
}
