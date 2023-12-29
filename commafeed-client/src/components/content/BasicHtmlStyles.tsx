import { TypographyStylesProvider } from "@mantine/core"
import { type ReactNode } from "react"

/**
 * This component is used to provide basic styles to html typography elements.
 *
 * see https://mantine.dev/core/typography-styles-provider/
 */
export const BasicHtmlStyles = (props: { children: ReactNode }) => {
    return <TypographyStylesProvider pl={0}>{props.children}</TypographyStylesProvider>
}
