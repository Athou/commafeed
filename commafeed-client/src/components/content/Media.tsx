import { Box, TypographyStylesProvider } from "@mantine/core"
import { Constants } from "app/constants"
import { calculatePlaceholderSize } from "app/utils"
import { ImageWithPlaceholderWhileLoading } from "components/ImageWithPlaceholderWhileLoading"
import { Content } from "./Content"

export interface MediaProps {
    thumbnailUrl: string
    thumbnailWidth?: number
    thumbnailHeight?: number
    description?: string
}

export function Media(props: MediaProps) {
    const width = props.thumbnailWidth
    const height = props.thumbnailHeight
    const placeholderSize = calculatePlaceholderSize({
        width,
        height,
        maxWidth: Constants.layout.entryMaxWidth,
    })
    return (
        <TypographyStylesProvider>
            <ImageWithPlaceholderWhileLoading
                src={props.thumbnailUrl}
                alt="media thumbnail"
                width={props.thumbnailWidth}
                height={props.thumbnailHeight}
                placeholderWidth={placeholderSize.width}
                placeholderHeight={placeholderSize.height}
            />
            {props.description && (
                <Box pt="md">
                    <Content content={props.description} />
                </Box>
            )}
        </TypographyStylesProvider>
    )
}
