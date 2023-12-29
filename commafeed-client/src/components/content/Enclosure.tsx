import { BasicHtmlStyles } from "components/content/BasicHtmlStyles"
import { ImageWithPlaceholderWhileLoading } from "components/ImageWithPlaceholderWhileLoading"

export function Enclosure(props: { enclosureType: string; enclosureUrl: string }) {
    const hasVideo = props.enclosureType?.startsWith("video")
    const hasAudio = props.enclosureType?.startsWith("audio")
    const hasImage = props.enclosureType?.startsWith("image")

    return (
        <BasicHtmlStyles>
            {hasVideo && (
                <video controls>
                    <source src={props.enclosureUrl} type={props.enclosureType} />
                </video>
            )}
            {hasAudio && (
                <audio controls>
                    <source src={props.enclosureUrl} type={props.enclosureType} />
                </audio>
            )}
            {hasImage && <ImageWithPlaceholderWhileLoading src={props.enclosureUrl} alt="enclosure" />}
        </BasicHtmlStyles>
    )
}
