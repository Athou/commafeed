import { Box, Center, type MantineTheme, useMantineTheme } from "@mantine/core"
import { FeedFavicon } from "components/content/FeedFavicon"
import { useColorScheme } from "hooks/useColorScheme"
import React, { type ReactNode } from "react"
import { tss } from "tss"
import { UnreadCount } from "./UnreadCount"

interface TreeNodeProps {
    id: string
    name: ReactNode
    icon: ReactNode
    unread: number
    selected: boolean
    expanded?: boolean
    level: number
    hasError: boolean
    onClick: (e: React.MouseEvent, id: string) => void
    onIconClick?: (e: React.MouseEvent, id: string) => void
}

const useStyles = tss
    .withParams<{
        theme: MantineTheme
        colorScheme: "dark" | "light"
        selected: boolean
        hasError: boolean
        hasUnread: boolean
    }>()
    .create(({ theme, colorScheme, selected, hasError, hasUnread }) => {
        let backgroundColor = "inherit"
        if (selected) backgroundColor = colorScheme === "dark" ? theme.colors.dark[4] : theme.colors.gray[3]

        let color
        if (hasError) {
            color = theme.colors.red[6]
        } else if (colorScheme === "dark") {
            color = hasUnread ? theme.colors.dark[0] : theme.colors.dark[3]
        } else {
            color = hasUnread ? theme.black : theme.colors.gray[6]
        }

        return {
            node: {
                display: "flex",
                alignItems: "center",
                cursor: "pointer",
                color,
                backgroundColor,
                "&:hover": {
                    backgroundColor: colorScheme === "dark" ? theme.colors.dark[6] : theme.colors.gray[0],
                },
            },
            nodeText: {
                flexGrow: 1,
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis",
            },
        }
    })

export function TreeNode(props: TreeNodeProps) {
    const theme = useMantineTheme()
    const colorScheme = useColorScheme()
    const { classes } = useStyles({
        theme,
        colorScheme,
        selected: props.selected,
        hasError: props.hasError,
        hasUnread: props.unread > 0,
    })
    return (
        <Box py={1} pl={props.level * 20} className={classes.node} onClick={(e: React.MouseEvent) => props.onClick(e, props.id)}>
            <Box mr={6} onClick={(e: React.MouseEvent) => props.onIconClick?.(e, props.id)}>
                <Center>{typeof props.icon === "string" ? <FeedFavicon url={props.icon} /> : props.icon}</Center>
            </Box>
            <Box className={classes.nodeText}>{props.name}</Box>
            {!props.expanded && (
                <Box>
                    <UnreadCount unreadCount={props.unread} />
                </Box>
            )}
        </Box>
    )
}
