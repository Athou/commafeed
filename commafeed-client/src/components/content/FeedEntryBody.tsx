import { Box } from "@mantine/core"
import { useAppSelector } from "app/store"
import type { Entry } from "app/types"
import { Content } from "./Content"
import { Enclosure } from "./Enclosure"
import { Media } from "./Media"

export interface FeedEntryBodyProps {
    entry: Entry
}

export function FeedEntryBody(props: FeedEntryBodyProps) {
    const search = useAppSelector(state => state.entries.search)
    return (
        <Box className={"cf-FeedEntryBody-Box"}>
            <Box className={"cf-FeedEntryBody-Box-Box"}>
                <Content content={props.entry.content} highlight={search} />
            </Box>
            {props.entry.enclosureType && props.entry.enclosureUrl && (
                <Box className={"cf-FeedEntryBody-Box-EnclosureBox"} pt="md">
                    <Enclosure enclosureType={props.entry.enclosureType} enclosureUrl={props.entry.enclosureUrl} />
                </Box>
            )}
            {/* show media only if we don't have content to avoid duplicate content */}
            {!props.entry.content && props.entry.mediaThumbnailUrl && (
                <Box className={"cf-FeedEntryBody-Box-MediaBox"} pt="md">
                    <Media
                        thumbnailUrl={props.entry.mediaThumbnailUrl}
                        thumbnailWidth={props.entry.mediaThumbnailWidth}
                        thumbnailHeight={props.entry.mediaThumbnailHeight}
                        description={props.entry.mediaDescription}
                    />
                </Box>
            )}
        </Box>
    )
}
