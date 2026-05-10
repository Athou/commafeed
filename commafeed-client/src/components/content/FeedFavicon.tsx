import { useTimeout } from "@mantine/hooks"
import { useState } from "react"
import { ImageWithPlaceholderWhileLoading } from "@/components/ImageWithPlaceholderWhileLoading"

export interface FeedFaviconProps {
    url: string
    size?: number
}

export function FeedFavicon({ url, size = 18 }: Readonly<FeedFaviconProps>) {
    // the backend always returns a favicon except when the feed has never been fetched
    // this can happen when the user subscribes to a feed, the feed is added to the tree but the feed has not been fetched yet
    // in this case we just retry every second until the feed is fetched and the favicon is available
    const [timestamp, setTimestamp] = useState(0)
    const { start: retry } = useTimeout(() => setTimestamp(Date.now()), 1000)

    const urlWithTimestamp = url + (timestamp === 0 ? "" : `?t=${timestamp}`)
    return (
        <ImageWithPlaceholderWhileLoading
            src={urlWithTimestamp}
            alt="feed favicon"
            width={size}
            height={size}
            placeholderWidth={size}
            placeholderHeight={size}
            placeholderBackgroundColor="inherit"
            placeholderIconSize={size}
            onError={retry}
        />
    )
}
