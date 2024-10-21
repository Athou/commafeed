import { Box, Mark } from "@mantine/core"
import { Constants } from "app/constants"
import { calculatePlaceholderSize } from "app/utils"
import { ImageWithPlaceholderWhileLoading } from "components/ImageWithPlaceholderWhileLoading"
import { BasicHtmlStyles } from "components/content/BasicHtmlStyles"
import escapeStringRegexp from "escape-string-regexp"
import { type ChildrenNode, Interweave, type MatchResponse, Matcher, type Node, type TransformCallback } from "interweave"
import React from "react"
import styleToObject from "style-to-object"
import { tss } from "tss"

export interface ContentProps {
    content: string
    highlight?: string
}

const useStyles = tss.create(() => ({
    content: {
        // break long links or long words
        overflowWrap: "anywhere",
        "& a": {
            color: "inherit",
            textDecoration: "underline",
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
        const width = nodeWidth ? Number.parseInt(nodeWidth, 10) : undefined
        const height = nodeHeight ? Number.parseInt(nodeHeight, 10) : undefined
        const style = styleToObject(node.getAttribute("style") ?? "") ?? undefined
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
                style={style}
                placeholderWidth={placeholderSize.width}
                placeholderHeight={placeholderSize.height}
            />
        )
    }
    return undefined
}

class HighlightMatcher extends Matcher {
    private readonly search: string

    constructor(search: string) {
        super("highlight")
        this.search = escapeStringRegexp(search)
    }

    match(string: string): MatchResponse<unknown> | null {
        const pattern = this.search.split(" ").join("|")
        return this.doMatch(string, new RegExp(pattern, "i"), () => ({}))
    }

    replaceWith(children: ChildrenNode): Node {
        return <Mark>{children}</Mark>
    }

    asTag(): string {
        return "span"
    }
}

// memoize component because Interweave is costly
const Content = React.memo((props: ContentProps) => {
    const { classes } = useStyles()
    const matchers = props.highlight ? [new HighlightMatcher(props.highlight)] : []

    return (
        <BasicHtmlStyles>
            <Box className={classes.content}>
                <Interweave content={props.content} transform={transform} matchers={matchers} />
            </Box>
        </BasicHtmlStyles>
    )
})
Content.displayName = "Content"

export { Content }
