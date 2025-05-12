import { Badge, Indicator, Tooltip } from "@mantine/core"
import { Constants } from "app/constants"
import { tss } from "tss"

const useStyles = tss.create(() => ({
    badge: {
        width: "3.2rem",
        cursor: "pointer",
    },
}))

export function UnreadCount(props: { unreadCount: number, newMessages: boolean | undefined }) {
    const { classes } = useStyles()

    if (props.unreadCount <= 0) return null

    const count = props.unreadCount >= 10000 ? "10k+" : props.unreadCount

    return (
        <Tooltip label={props.unreadCount} disabled={props.unreadCount === count} openDelay={Constants.tooltip.delay}>
            <Indicator disabled={!props.newMessages} size={4} offset={10} position="top-start" color="orange" withBorder={false} zIndex={5}>
                <Badge className={`${classes.badge} cf-badge`} variant="light" fullWidth>
                    {count}
                </Badge>
            </Indicator>
        </Tooltip>
    )
}
