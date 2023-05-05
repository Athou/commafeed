import { Trans } from "@lingui/macro"
import { Box, Button, Group, Stack, Textarea } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { redirectToSelectedSource } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { Alert } from "components/Alert"
import { useEffect } from "react"
import { useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy } from "react-icons/tb"

interface FormData {
    customCss: string
    customJs: string
}

export function CustomCodeSettings() {
    const settings = useAppSelector(state => state.user.settings)
    const dispatch = useAppDispatch()

    const form = useForm<FormData>()
    const { setValues } = form

    const saveCustomCode = useAsyncCallback(
        async (d: FormData) => {
            if (!settings) return
            await client.user.saveSettings({
                ...settings,
                customCss: d.customCss,
                customJs: d.customJs,
            })
        },
        {
            onSuccess: () => {
                window.location.reload()
            },
        }
    )

    useEffect(() => {
        if (!settings) return
        setValues({
            customCss: settings.customCss,
            customJs: settings.customJs,
        })
    }, [setValues, settings])

    return (
        <>
            {saveCustomCode.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(saveCustomCode.error)} />
                </Box>
            )}

            <form onSubmit={form.onSubmit(saveCustomCode.execute)}>
                <Stack>
                    <Textarea
                        autosize
                        minRows={4}
                        maxRows={15}
                        {...form.getInputProps("customCss")}
                        description={<Trans>Custom CSS rules that will be applied</Trans>}
                        styles={{
                            input: {
                                fontFamily: "monospace",
                            },
                        }}
                    />

                    <Textarea
                        autosize
                        minRows={4}
                        maxRows={15}
                        {...form.getInputProps("customJs")}
                        description={<Trans>Custom JS code that will be executed on page load</Trans>}
                        styles={{
                            input: {
                                fontFamily: "monospace",
                            },
                        }}
                    />

                    <Group>
                        <Button variant="default" onClick={() => dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftIcon={<TbDeviceFloppy size={16} />} loading={saveCustomCode.loading}>
                            <Trans>Save</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
