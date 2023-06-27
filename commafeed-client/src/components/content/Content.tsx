import { Box, createStyles, Mark, TypographyStylesProvider } from "@mantine/core"
import { Constants } from "app/constants"
import { calculatePlaceholderSize } from "app/utils"
import { ImageWithPlaceholderWhileLoading } from "components/ImageWithPlaceholderWhileLoading"
import escapeStringRegexp from "escape-string-regexp"
import { ChildrenNode, Interweave, Matcher, MatchResponse, Node, TransformCallback } from "interweave"
import React from "react"

export interface ContentProps {
    content: string
    highlight?: string
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
        if (!src) return undefined

        const alt = node.getAttribute("alt") ?? "image"
        const title = node.getAttribute("title") ?? undefined
        const nodeWidth = node.getAttribute("width")
        const nodeHeight = node.getAttribute("height")
        const width = nodeWidth ? parseInt(nodeWidth, 10) : undefined
        const height = nodeHeight ? parseInt(nodeHeight, 10) : undefined
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

class HighlightMatcher extends Matcher {
    private search: string

    constructor(search: string) {
        super("highlight")
        this.search = escapeStringRegexp(search)
    }

    match(string: string): MatchResponse<unknown> | null {
        const pattern = this.search.split(" ").join("|")
        return this.doMatch(string, new RegExp(pattern, "i"), () => ({}))
    }

    // eslint-disable-next-line class-methods-use-this, @typescript-eslint/no-unused-vars
    replaceWith(children: ChildrenNode, props: unknown): Node {
        return <Mark>{children}</Mark>
    }

    // eslint-disable-next-line class-methods-use-this
    asTag(): string {
        return "span"
    }
}

// memoize component because Interweave is costly
const Content = React.memo((props: ContentProps) => {
    const { classes } = useStyles()
    const matchers = props.highlight ? [new HighlightMatcher(props.highlight)] : []

    return (
        <TypographyStylesProvider>
            <Box className={classes.content}>
                <Interweave content={props.content} transform={transform} matchers={matchers} />
            </Box>
        </TypographyStylesProvider>
    )
})

export { Content }
