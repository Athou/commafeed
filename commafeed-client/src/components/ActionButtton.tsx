import { ActionIcon, Button, useMantineTheme } from "@mantine/core"
import { useMediaQuery } from "@mantine/hooks"
import { forwardRef } from "react"

interface ActionButtonProps {
    className?: string
    icon?: React.ReactNode
    label?: string
    onClick?: React.MouseEventHandler
}

/**
 * Switches between Button with label (desktop) and ActionIcon (mobile)
 */
export const ActionButton = forwardRef<HTMLButtonElement, ActionButtonProps>((props: ActionButtonProps, ref) => {
    const theme = useMantineTheme()
    const mobile = !useMediaQuery(`(min-width: ${theme.breakpoints.lg}px)`)
    return mobile ? (
        <ActionIcon ref={ref} color={theme.primaryColor} variant="subtle" className={props.className} onClick={props.onClick}>
            {props.icon}
        </ActionIcon>
    ) : (
        <Button ref={ref} variant="subtle" size="xs" className={props.className} leftIcon={props.icon} onClick={props.onClick}>
            {props.label}
        </Button>
    )
})
ActionButton.displayName = "HeaderButton"
