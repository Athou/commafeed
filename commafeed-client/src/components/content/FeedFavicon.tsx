import { ImageWithPlaceholderWhileLoading } from "@/components/ImageWithPlaceholderWhileLoading"

export interface FeedFaviconProps {
    url: string
    size?: number
}

export function FeedFavicon({ url, size = 18 }: FeedFaviconProps) {
    return (
        <ImageWithPlaceholderWhileLoading
            src={url}
            alt="feed favicon"
            width={size}
            height={size}
            placeholderWidth={size}
            placeholderHeight={size}
            placeholderBackgroundColor="inherit"
            placeholderIconSize={size}
        />
    )
}
