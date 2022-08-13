import { createStyles } from "@mantine/core"

const useStyles = createStyles(() => ({
    enclosureImage: {
        maxWidth: "100%",
        height: "auto",
    },
}))

export function Enclosure(props: { enclosureType?: string; enclosureUrl?: string }) {
    const { classes } = useStyles()
    const hasVideo = props.enclosureType && props.enclosureType.indexOf("video") === 0
    const hasAudio = props.enclosureType && props.enclosureType.indexOf("audio") === 0
    const hasImage = props.enclosureType && props.enclosureType.indexOf("image") === 0
    return (
        <>
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
            {hasImage && <img src={props.enclosureUrl} alt="enclosure" className={classes.enclosureImage} />}
        </>
    )
}
