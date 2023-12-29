import { Box, Center, type MantineTheme, useMantineTheme } from "@mantine/core"
import { useState } from "react"
import { TbPhoto } from "react-icons/tb"
import { tss } from "tss"

interface ImageWithPlaceholderWhileLoadingProps {
    src: string
    alt: string
    title?: string
    width?: number
    height?: number | "auto"
    placeholderWidth?: number
    placeholderHeight?: number
    placeholderBackgroundColor?: string
    placeholderIconSize?: number
    placeholderIconColor?: string
}

const useStyles = tss
    .withParams<{
        theme: MantineTheme
        placeholderWidth?: number
        placeholderHeight?: number
        placeholderBackgroundColor?: string
        placeholderIconColor?: string
    }>()
    .create(props => ({
        placeholder: {
            width: props.placeholderWidth ?? 400,
            height: props.placeholderHeight ?? 600,
            maxWidth: "100%",
            color:
                props.placeholderIconColor ??
                props.theme.fn.variant({
                    color: props.theme.primaryColor,
                    variant: "subtle",
                }).color,
            backgroundColor:
                props.placeholderBackgroundColor ??
                (props.theme.colorScheme === "dark" ? props.theme.colors.dark[5] : props.theme.colors.gray[1]),
        },
    }))

export function ImageWithPlaceholderWhileLoading({
    alt,
    height,
    placeholderBackgroundColor,
    placeholderHeight,
    placeholderIconColor,
    placeholderIconSize,
    placeholderWidth,
    src,
    title,
    width,
}: ImageWithPlaceholderWhileLoadingProps) {
    const theme = useMantineTheme()
    const { classes } = useStyles({
        theme,
        placeholderWidth,
        placeholderHeight,
        placeholderBackgroundColor,
        placeholderIconColor,
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
                style={{ display: loading ? "none" : "block" }}
            />
        </>
    )
}
