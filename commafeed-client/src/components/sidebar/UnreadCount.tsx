import { Badge, Tooltip } from "@mantine/core"
import { Constants } from "app/constants"
import { tss } from "tss"

const useStyles = tss.create(() => ({
    badge: {
        width: "3.2rem",
        // for some reason, mantine Badge has "cursor: 'default'"
        cursor: "pointer",
    },
}))

export function UnreadCount(props: { unreadCount: number }) {
    const { classes } = useStyles()

    if (props.unreadCount <= 0) return null

    const count = props.unreadCount >= 10000 ? "10k+" : props.unreadCount
    return (
        <Tooltip label={props.unreadCount} disabled={props.unreadCount === count} openDelay={Constants.tooltip.delay}>
            <Badge className={`${classes.badge} cf-badge`} variant="light" fullWidth>
                {count}
            </Badge>
        </Tooltip>
    )
}
