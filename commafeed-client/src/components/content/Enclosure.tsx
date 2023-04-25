import { TypographyStylesProvider } from "@mantine/core"
import { ImageWithPlaceholderWhileLoading } from "components/ImageWithPlaceholderWhileLoading"

export function Enclosure(props: { enclosureType: string; enclosureUrl: string }) {
    const hasVideo = props.enclosureType && props.enclosureType.indexOf("video") === 0
    const hasAudio = props.enclosureType && props.enclosureType.indexOf("audio") === 0
    const hasImage = props.enclosureType && props.enclosureType.indexOf("image") === 0

    return (
        <TypographyStylesProvider>
            {hasVideo && (
                // eslint-disable-next-line jsx-a11y/media-has-caption
                <video controls>
                    <source src={props.enclosureUrl} type={props.enclosureType} />
                </video>
            )}
            {hasAudio && (
                // eslint-disable-next-line jsx-a11y/media-has-caption
                <audio controls>
                    <source src={props.enclosureUrl} type={props.enclosureType} />
                </audio>
            )}
            {hasImage && <ImageWithPlaceholderWhileLoading src={props.enclosureUrl} alt="enclosure" />}
        </TypographyStylesProvider>
    )
}
