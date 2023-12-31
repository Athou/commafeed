import { Box, Center, type MantineTheme, useMantineTheme } from "@mantine/core"
import { useColorScheme } from "hooks/useColorScheme"
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
}

const useStyles = tss
    .withParams<{
        theme: MantineTheme
        colorScheme: "light" | "dark"
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
}: ImageWithPlaceholderWhileLoadingProps) {
    const theme = useMantineTheme()
    const colorScheme = useColorScheme()
    const { classes } = useStyles({
        theme,
        colorScheme,
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
                style={{ display: loading ? "none" : "block" }}
            />
        </>
    )
}
