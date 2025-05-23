import { Badge, Indicator, Tooltip } from "@mantine/core"
import { Constants } from "app/constants"
import { tss } from "tss"

const useStyles = tss.create(() => ({
    badge: {
        width: "3.2rem",
        // for some reason, mantine Badge has "cursor: 'default'"
        cursor: "pointer",
    },
}))

export function UnreadCount(props: { unreadCount: number; showIndicator: boolean }) {
    const { classes } = useStyles()

    if (props.unreadCount <= 0) return null

    const count = props.unreadCount >= 10000 ? "10k+" : props.unreadCount
    return (
        <Tooltip label={props.unreadCount} disabled={props.unreadCount === count} openDelay={Constants.tooltip.delay}>
            <Indicator disabled={!props.showIndicator} size={4} offset={10} position="middle-start">
                <Badge className={`${classes.badge} cf-badge`} variant="light" fullWidth>
                    {count}
                </Badge>
            </Indicator>
        </Tooltip>
    )
}
