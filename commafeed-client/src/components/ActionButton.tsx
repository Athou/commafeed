import type { MessageDescriptor } from "@lingui/core"
import { useLingui } from "@lingui/react"
import { ActionIcon, Button, type ButtonVariant, Tooltip, useMantineTheme } from "@mantine/core"
import type { ActionIconVariant } from "@mantine/core/lib/components/ActionIcon/ActionIcon"
import { Constants } from "app/constants"
import { useActionButton } from "hooks/useActionButton"
import { type MouseEventHandler, type ReactNode, forwardRef } from "react"

interface ActionButtonProps {
    className?: string
    icon?: ReactNode
    label?: string | MessageDescriptor
    onClick?: MouseEventHandler
    variant?: ActionIconVariant & ButtonVariant
    hideLabelOnDesktop?: boolean
    showLabelOnMobile?: boolean
}

/**
 * Switches between Button with label (desktop) and ActionIcon (mobile)
 */
export const ActionButton = forwardRef<HTMLButtonElement, ActionButtonProps>((props: ActionButtonProps, ref) => {
    const { mobile } = useActionButton()
    const theme = useMantineTheme()
    const { _ } = useLingui()

    const label = typeof props.label === "string" ? props.label : props.label && _(props.label)
    const variant = props.variant ?? "subtle"
    const iconOnly = (mobile && !props.showLabelOnMobile) || (!mobile && props.hideLabelOnDesktop)
    return iconOnly ? (
        <Tooltip label={label} openDelay={Constants.tooltip.delay}>
            <ActionIcon
                ref={ref}
                color={theme.primaryColor}
                variant={variant}
                className={props.className}
                onClick={props.onClick}
                aria-label={label}
            >
                {props.icon}
            </ActionIcon>
        </Tooltip>
    ) : (
        <Button
            ref={ref}
            variant={variant}
            size="xs"
            className={props.className}
            leftSection={props.icon}
            onClick={props.onClick}
            aria-label={label}
        >
            {label}
        </Button>
    )
})
ActionButton.displayName = "HeaderButton"
