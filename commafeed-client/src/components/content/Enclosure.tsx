import { BasicHtmlStyles } from "@/components/content/BasicHtmlStyles"
import { ImageWithPlaceholderWhileLoading } from "@/components/ImageWithPlaceholderWhileLoading"

export function Enclosure(
    props: Readonly<{
        enclosureType: string
        enclosureUrl: string
    }>
) {
    const hasVideo = props.enclosureType.startsWith("video")
    const hasAudio = props.enclosureType.startsWith("audio")
    const hasImage = props.enclosureType.startsWith("image")

    return (
        <BasicHtmlStyles>
            {hasVideo && (
                // biome-ignore lint/a11y/useMediaCaption: we don't have any captions for videos
                <video controls width="100%">
                    <source src={props.enclosureUrl} type={props.enclosureType} />
                </video>
            )}
            {hasAudio && (
                // biome-ignore lint/a11y/useMediaCaption: we don't have any captions for audio
                <audio controls style={{ width: "100%" }}>
                    <source src={props.enclosureUrl} type={props.enclosureType} />
                </audio>
            )}
            {hasImage && <ImageWithPlaceholderWhileLoading src={props.enclosureUrl} alt="enclosure" />}
        </BasicHtmlStyles>
    )
}
