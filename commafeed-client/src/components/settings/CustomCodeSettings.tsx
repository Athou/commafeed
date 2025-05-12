import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Button, Group, Stack } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { Constants } from "app/constants"
import { redirectToSelectedSource } from "app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import { Alert } from "components/Alert"
import { CodeEditor } from "components/code/CodeEditor"
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
                    <CodeEditor
                        label={<Trans>Custom CSS rules that will be applied</Trans>}
                        description={
                            <Trans>
                                <span>See </span>
                                <Anchor
                                    href={Constants.customCssDocumentationUrl}
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{ fontSize: "inherit" }}
                                >
                                    here
                                </Anchor>
                                <span> for more information.</span>
                            </Trans>
                        }
                        language="css"
                        {...form.getInputProps("customCss")}
                    />

                    <CodeEditor
                        label={<Trans>Custom JS code that will be executed on page load</Trans>}
                        language="javascript"
                        {...form.getInputProps("customJs")}
                    />

                    <Group>
                        <Button variant="default" onClick={async () => await dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftSection={<TbDeviceFloppy size={16} />} loading={saveCustomCode.loading}>
                            <Trans>Save</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
