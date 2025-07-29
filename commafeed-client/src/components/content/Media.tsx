import { Box } from "@mantine/core"
import { Constants } from "@/app/constants"
import { calculatePlaceholderSize } from "@/app/utils"
import { BasicHtmlStyles } from "@/components/content/BasicHtmlStyles"
import { ImageWithPlaceholderWhileLoading } from "@/components/ImageWithPlaceholderWhileLoading"
import { Content } from "./Content"

export interface MediaProps {
    thumbnailUrl: string
    thumbnailWidth?: number
    thumbnailHeight?: number
    description?: string
}

export function Media(props: Readonly<MediaProps>) {
    const width = props.thumbnailWidth
    const height = props.thumbnailHeight
    const placeholderSize = calculatePlaceholderSize({
        width,
        height,
        maxWidth: Constants.layout.entryMaxWidth,
    })
    return (
        <BasicHtmlStyles>
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
        </BasicHtmlStyles>
    )
}
