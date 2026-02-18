import { Trans } from "@lingui/react/macro"
import { Checkbox, type CheckboxProps } from "@mantine/core"
import type { ReactNode } from "react"
import { useAppSelector } from "@/app/store"

export const ReceivePushNotificationsChechbox = (props: CheckboxProps) => {
    const pushNotificationsEnabled = useAppSelector(state => state.server.serverInfos?.pushNotificationsEnabled)
    const pushNotificationsConfigured = useAppSelector(state => !!state.user.settings?.pushNotificationSettings.type)

    const disabled = !pushNotificationsEnabled || !pushNotificationsConfigured
    let description: ReactNode = ""
    if (!pushNotificationsEnabled) {
        description = <Trans>Push notifications are not enabled on this CommaFeed instance.</Trans>
    } else if (!pushNotificationsConfigured) {
        description = <Trans>Push notifications are not configured in your user settings.</Trans>
    }

    return <Checkbox label={<Trans>Receive push notifications</Trans>} disabled={disabled} description={description} {...props} />
}
