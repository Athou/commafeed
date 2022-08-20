import { Box, createStyles, TypographyStylesProvider } from "@mantine/core"
import { Constants } from "app/constants"
import { calculatePlaceholderSize } from "app/utils"
import { ImageWithPlaceholderWhileLoading } from "components/ImageWithPlaceholderWhileLoading"
import { Interweave, TransformCallback } from "interweave"

export interface ContentProps {
    content: string
}

const useStyles = createStyles(theme => ({
    content: {
        // break long links or long words
        overflowWrap: "anywhere",
        "& a": {
            color: theme.fn.variant({ color: theme.primaryColor, variant: "subtle" }).color,
        },
        "& iframe": {
            maxWidth: "100%",
        },
        "& pre, & code": {
            whiteSpace: "pre-wrap",
        },
    },
}))

const transform: TransformCallback = node => {
    if (node.tagName === "IMG") {
        // show placeholders for loading img tags, this allows the entry to have its final height immediately
        const src = node.getAttribute("src") ?? undefined
        const alt = node.getAttribute("alt") ?? undefined
        const title = node.getAttribute("title") ?? undefined
        const width = node.getAttribute("width") ? parseInt(node.getAttribute("width")!, 10) : undefined
        const height = node.getAttribute("height") ? parseInt(node.getAttribute("height")!, 10) : undefined
        const placeholderSize = calculatePlaceholderSize({
            width,
            height,
            maxWidth: Constants.layout.entryMaxWidth,
        })
        return (
            <ImageWithPlaceholderWhileLoading
                src={src}
                alt={alt}
                title={title}
                width={width}
                height="auto"
                placeholderWidth={placeholderSize.width}
                placeholderHeight={placeholderSize.height}
            />
        )
    }
    return undefined
}

export function Content(props: ContentProps) {
    const { classes } = useStyles()

    return (
        <TypographyStylesProvider>
            <Box className={classes.content}>
                <Interweave content={props.content} transform={transform} />
            </Box>
        </TypographyStylesProvider>
    )
}
