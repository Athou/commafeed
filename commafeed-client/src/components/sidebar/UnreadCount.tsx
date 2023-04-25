import { Badge, createStyles } from "@mantine/core"

const useStyles = createStyles(() => ({
    badge: {
        width: "3.2rem",
        // for some reason, mantine Badge has "cursor: 'default'"
        cursor: "pointer",
    },
}))

export function UnreadCount(props: { unreadCount: number }) {
    const { classes } = useStyles()

    if (props.unreadCount <= 0) return null

    const count = props.unreadCount >= 1000 ? "999+" : props.unreadCount
    return <Badge className={classes.badge}>{count}</Badge>
}
