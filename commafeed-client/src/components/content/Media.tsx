import { Box, createStyles } from "@mantine/core"
import { Content } from "./Content"

export interface MediaProps {
    thumbnailUrl: string
    thumbnailWidth?: number
    thumbnailHeight?: number
    description?: string
}

const useStyles = createStyles(() => ({
    image: {
        maxWidth: "100%",
        height: "auto",
    },
}))

export function Media(props: MediaProps) {
    const { classes } = useStyles()
    return (
        <>
            <img
                className={classes.image}
                src={props.thumbnailUrl}
                width={props.thumbnailWidth}
                height={props.thumbnailHeight}
                alt="media thumbnail"
            />
            {props.description && (
                <Box pt="md">
                    <Content content={props.description} />
                </Box>
            )}
        </>
    )
}
