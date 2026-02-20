import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Button, Divider, Group, Select, Stack, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useEffect } from "react"
import { useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy, TbSend } from "react-icons/tb"
import { client, errorToStrings } from "@/app/client"
import { redirectToSelectedSource } from "@/app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { PushNotificationSettings as PushNotificationSettingsModel } from "@/app/types"
import { changePushNotificationSettings } from "@/app/user/thunks"
import { Alert } from "@/components/Alert"

export function PushNotificationSettings() {
    const notificationSettings = useAppSelector(state => state.user.settings?.pushNotificationSettings)
    const pushNotificationsEnabled = useAppSelector(state => state.server.serverInfos?.pushNotificationsEnabled)
    const { _ } = useLingui()
    const dispatch = useAppDispatch()

    const form = useForm<PushNotificationSettingsModel>()
    useEffect(() => {
        if (notificationSettings) form.initialize(notificationSettings)
    }, [form.initialize, notificationSettings])

    const handleSubmit = (values: PushNotificationSettingsModel) => {
        dispatch(changePushNotificationSettings(values))
    }

    const sendTestPushNotification = useAsyncCallback(client.user.sendTestPushNotification)

    const typeInputProps = form.getInputProps("type")

    if (!pushNotificationsEnabled) {
        return <Trans>Push notifications are not enabled on this CommaFeed instance.</Trans>
    }
    return (
        <form onSubmit={form.onSubmit(handleSubmit)}>
            <Stack>
                {sendTestPushNotification.status === "success" && (
                    <Alert level="success" messages={[_(msg`Test notification sent successfully.`)]} />
                )}

                {sendTestPushNotification.status === "error" && (
                    <Alert level="error" messages={errorToStrings(sendTestPushNotification.error)} />
                )}

                <Select
                    label={<Trans>Push notification service</Trans>}
                    description={
                        <Trans>
                            Receive push notifications when new feed entries are discovered. Enable "Receive push notifications" in the
                            settings of each feed for which you want to receive notifications.
                        </Trans>
                    }
                    data={[
                        { value: "ntfy", label: "ntfy" },
                        { value: "gotify", label: "Gotify" },
                        { value: "pushover", label: "Pushover" },
                    ]}
                    clearable
                    {...typeInputProps}
                    onChange={value => {
                        typeInputProps.onChange(value)
                        form.setFieldValue("serverUrl", "")
                        form.setFieldValue("topic", "")
                        form.setFieldValue("userSecret", "")
                        form.setFieldValue("userId", "")
                    }}
                />
                {form.values.type === "ntfy" && (
                    <>
                        <TextInput
                            label={<Trans>Server URL</Trans>}
                            placeholder="https://ntfy.sh"
                            required
                            {...form.getInputProps("serverUrl")}
                        />
                        <TextInput label={<Trans>Topic</Trans>} placeholder="commafeed" required {...form.getInputProps("topic")} />
                        <TextInput label={<Trans>Access token</Trans>} {...form.getInputProps("userSecret")} />
                    </>
                )}
                {form.values.type === "gotify" && (
                    <>
                        <TextInput
                            label={<Trans>Server URL</Trans>}
                            placeholder="https://gotify.example.com"
                            required
                            {...form.getInputProps("serverUrl")}
                        />
                        <TextInput label={<Trans>App token</Trans>} required {...form.getInputProps("userSecret")} />
                    </>
                )}
                {form.values.type === "pushover" && (
                    <>
                        <TextInput label={<Trans>User key</Trans>} required {...form.getInputProps("userId")} />
                        <TextInput label={<Trans>API token</Trans>} required {...form.getInputProps("userSecret")} />
                    </>
                )}

                <Group>
                    <Button variant="default" onClick={async () => await dispatch(redirectToSelectedSource())}>
                        <Trans>Cancel</Trans>
                    </Button>
                    <Button type="submit" leftSection={<TbDeviceFloppy size={16} />}>
                        <Trans>Save</Trans>
                    </Button>

                    <Divider orientation="vertical" />

                    <Button
                        variant="outline"
                        leftSection={<TbSend size={16} />}
                        onClick={() => sendTestPushNotification.execute(form.values)}
                        loading={sendTestPushNotification.loading}
                    >
                        <Trans>Test</Trans>
                    </Button>
                </Group>
            </Stack>
        </form>
    )
}
