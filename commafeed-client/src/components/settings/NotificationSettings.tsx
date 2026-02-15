import { Trans } from "@lingui/react/macro"
import { Divider, Select, Stack, TextInput } from "@mantine/core"
import { useEffect, useState } from "react"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { NotificationService as NotificationServiceType, NotificationSettings as NotificationSettingsType } from "@/app/types"
import { changeNotificationSettings } from "@/app/user/thunks"

function useDebouncedSave(value: string, settingsKey: string, dispatch: ReturnType<typeof useAppDispatch>) {
    const [localValue, setLocalValue] = useState(value)

    useEffect(() => {
        setLocalValue(value)
    }, [value])

    const onBlur = async () => {
        if (localValue !== value) {
            await dispatch(changeNotificationSettings({ [settingsKey]: localValue }))
        }
    }

    return { localValue, setLocalValue, onBlur }
}

function toServiceValue(settings?: NotificationSettingsType): NotificationServiceType {
    if (settings?.enabled && settings.type) {
        return settings.type
    }
    return "disabled"
}

export function NotificationSettings() {
    const notificationSettings = useAppSelector(state => state.user.settings?.notificationSettings)
    const dispatch = useAppDispatch()

    const serviceValue = toServiceValue(notificationSettings)

    const serverUrl = useDebouncedSave(notificationSettings?.serverUrl ?? "", "serverUrl", dispatch)
    const token = useDebouncedSave(notificationSettings?.token ?? "", "token", dispatch)
    const userKey = useDebouncedSave(notificationSettings?.userKey ?? "", "userKey", dispatch)
    const topic = useDebouncedSave(notificationSettings?.topic ?? "", "topic", dispatch)

    const onServiceChange = async (value: string | null) => {
        if (value === "disabled" || !value) {
            await dispatch(changeNotificationSettings({ enabled: false, type: undefined }))
        } else {
            await dispatch(changeNotificationSettings({ enabled: true, type: value as Exclude<NotificationServiceType, "disabled"> }))
        }
    }

    return (
        <Stack>
            <Divider
                label={
                    <Trans>
                        <b>Notifications</b>
                    </Trans>
                }
            />

            <Select
                label={<Trans>Notification service</Trans>}
                data={[
                    { value: "disabled", label: "Disabled" },
                    { value: "ntfy", label: "ntfy" },
                    { value: "gotify", label: "Gotify" },
                    { value: "pushover", label: "Pushover" },
                ]}
                value={serviceValue}
                onChange={onServiceChange}
            />

            {serviceValue === "ntfy" && (
                <>
                    <TextInput
                        label={<Trans>Server URL</Trans>}
                        placeholder="https://ntfy.sh"
                        value={serverUrl.localValue}
                        onChange={e => serverUrl.setLocalValue(e.currentTarget.value)}
                        onBlur={serverUrl.onBlur}
                    />
                    <TextInput
                        label={<Trans>Topic</Trans>}
                        placeholder="commafeed"
                        value={topic.localValue}
                        onChange={e => topic.setLocalValue(e.currentTarget.value)}
                        onBlur={topic.onBlur}
                    />
                    <TextInput
                        label={<Trans>Access token (optional)</Trans>}
                        value={token.localValue}
                        onChange={e => token.setLocalValue(e.currentTarget.value)}
                        onBlur={token.onBlur}
                    />
                </>
            )}

            {serviceValue === "gotify" && (
                <>
                    <TextInput
                        label={<Trans>Server URL</Trans>}
                        placeholder="https://gotify.example.com"
                        value={serverUrl.localValue}
                        onChange={e => serverUrl.setLocalValue(e.currentTarget.value)}
                        onBlur={serverUrl.onBlur}
                    />
                    <TextInput
                        label={<Trans>App token</Trans>}
                        value={token.localValue}
                        onChange={e => token.setLocalValue(e.currentTarget.value)}
                        onBlur={token.onBlur}
                    />
                </>
            )}

            {serviceValue === "pushover" && (
                <>
                    <TextInput
                        label={<Trans>API token</Trans>}
                        value={token.localValue}
                        onChange={e => token.setLocalValue(e.currentTarget.value)}
                        onBlur={token.onBlur}
                    />
                    <TextInput
                        label={<Trans>User key</Trans>}
                        value={userKey.localValue}
                        onChange={e => userKey.setLocalValue(e.currentTarget.value)}
                        onBlur={userKey.onBlur}
                    />
                </>
            )}
        </Stack>
    )
}
