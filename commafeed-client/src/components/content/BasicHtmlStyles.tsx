import { TypographyStylesProvider } from "@mantine/core"
import type { ReactNode } from "react"
import { tss } from "@/tss"

/**
 * This component is used to provide basic styles to html typography elements.
 *
 * see https://mantine.dev/core/typography-styles-provider/
 */

const useStyles = tss.create(() => ({
    // override mantine default typography styles
    content: {
        paddingLeft: 0,
        "& img": {
            marginBottom: 0,
        },
    },
}))

export const BasicHtmlStyles = (props: { children: ReactNode }) => {
    const { classes } = useStyles()
    return <TypographyStylesProvider className={classes.content}>{props.children}</TypographyStylesProvider>
}
