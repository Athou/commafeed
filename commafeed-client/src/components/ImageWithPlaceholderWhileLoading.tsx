import { Box, Center, createStyles } from "@mantine/core"
import { useState } from "react"
import { TbPhoto } from "react-icons/tb"

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

const useStyles = createStyles((theme, props: ImageWithPlaceholderWhileLoadingProps) => ({
    placeholder: {
        width: props.placeholderWidth ?? 400,
        height: props.placeholderHeight ?? 600,
        maxWidth: "100%",
        color: props.placeholderIconColor ?? theme.fn.variant({ color: theme.primaryColor, variant: "subtle" }).color,
        backgroundColor: props.placeholderBackgroundColor ?? (theme.colorScheme === "dark" ? theme.colors.dark[5] : theme.colors.gray[1]),
    },
}))

export function ImageWithPlaceholderWhileLoading(props: ImageWithPlaceholderWhileLoadingProps) {
    const { classes } = useStyles(props)
    const [loading, setLoading] = useState(true)

    return (
        <>
            {loading && (
                <Box>
                    <Center className={classes.placeholder}>
                        <div>
                            <TbPhoto size={props.placeholderIconSize ?? 48} />
                        </div>
                    </Center>
                </Box>
            )}
            <img
                src={props.src}
                alt={props.alt}
                title={props.title}
                width={props.width}
                height={props.height}
                onLoad={() => setLoading(false)}
                style={{ display: loading ? "none" : "block" }}
            />
        </>
    )
}
