import { TypographyStylesProvider } from "@mantine/core"
import { ImageWithPlaceholderWhileLoading } from "components/ImageWithPlaceholderWhileLoading"

export function Enclosure(props: { enclosureType: string; enclosureUrl: string }) {
    const hasVideo = props.enclosureType && props.enclosureType.indexOf("video") === 0
    const hasAudio = props.enclosureType && props.enclosureType.indexOf("audio") === 0
    const hasImage = props.enclosureType && props.enclosureType.indexOf("image") === 0

    return (
        <TypographyStylesProvider>
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
        </TypographyStylesProvider>
    )
}
