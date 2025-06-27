import { Box, Center } from "@mantine/core"
import { useState } from "react"
import { TbPhoto } from "react-icons/tb"
import { tss } from "@/tss"

interface ImageWithPlaceholderWhileLoadingProps {
    src: string
    alt: string
    title?: string
    width?: number
    height?: number | "auto"
    style?: React.CSSProperties
    placeholderWidth?: number
    placeholderHeight?: number
    placeholderBackgroundColor?: string
    placeholderIconSize?: number
}

const useStyles = tss
    .withParams<{
        placeholderWidth?: number
        placeholderHeight?: number
        placeholderBackgroundColor?: string
    }>()
    .create(props => ({
        placeholder: {
            width: props.placeholderWidth ?? 400,
            height: props.placeholderHeight ?? 600,
            maxWidth: "100%",
            backgroundColor:
                props.placeholderBackgroundColor ??
                (props.colorScheme === "dark" ? props.theme.colors.dark[5] : props.theme.colors.gray[1]),
        },
    }))

export function ImageWithPlaceholderWhileLoading({
    alt,
    height,
    placeholderBackgroundColor,
    placeholderHeight,
    placeholderIconSize,
    placeholderWidth,
    src,
    title,
    width,
    style,
}: ImageWithPlaceholderWhileLoadingProps) {
    const { classes } = useStyles({
        placeholderWidth,
        placeholderHeight,
        placeholderBackgroundColor,
    })
    const [loading, setLoading] = useState(true)

    return (
        <>
            {loading && (
                <Box>
                    <Center className={classes.placeholder}>
                        <div>
                            <TbPhoto size={placeholderIconSize ?? 48} />
                        </div>
                    </Center>
                </Box>
            )}
            <img
                src={src}
                alt={alt}
                title={title}
                width={width}
                height={height}
                onLoad={() => setLoading(false)}
                style={{
                    ...style,
                    display: loading ? "none" : (style?.display ?? "initial"),
                    height: style?.width ? "auto" : style?.height,
                }}
            />
        </>
    )
}
