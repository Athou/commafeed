import { createStyles, Text } from "@mantine/core"

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
        "& img": {
            maxWidth: "100%",
            height: "auto",
        },
        "& iframe": {
            maxWidth: "100%",
        },
        "& pre, & code": {
            whiteSpace: "pre-wrap",
        },
    },
}))

export function Content(props: ContentProps) {
    const { classes } = useStyles()
    return <Text size="md" className={classes.content} dangerouslySetInnerHTML={{ __html: props.content }} />
}
