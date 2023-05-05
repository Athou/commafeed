import { Trans } from "@lingui/macro"
import { Button, Group, Stack, Textarea } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useEffect } from "react"
import { useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy } from "react-icons/tb"
import { client } from "../../app/client"
import { redirectToSelectedSource } from "../../app/slices/redirect"
import { useAppDispatch, useAppSelector } from "../../app/store"

interface FormData {
    customCss: string
}

export function CustomCss() {
    const settings = useAppSelector(state => state.user.settings)
    const customCss = settings?.customCss
    const dispatch = useAppDispatch()

    const form = useForm<FormData>()
    const { setValues } = form

    const saveCustomCss = useAsyncCallback(client.user.saveSettings, {
        onSuccess: () => {
            window.location.reload()
        },
    })

    useEffect(() => {
        if (!customCss) return
        setValues({
            customCss,
        })
    }, [setValues, customCss])

    return (
        <form
            onSubmit={form.onSubmit(data => {
                if (!settings) return
                saveCustomCss
                    .execute({
                        ...settings,
                        customCss: data.customCss,
                    })
                    .then(() => window.location.reload())
            })}
        >
            <Stack>
                <Textarea
                    autosize
                    minRows={4}
                    {...form.getInputProps("customCss")}
                    description={<Trans>Custom CSS rules that will be applied</Trans>}
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
                    <Button type="submit" leftIcon={<TbDeviceFloppy size={16} />} loading={saveCustomCss.loading}>
                        <Trans>Save</Trans>
                    </Button>
                </Group>
            </Stack>
        </form>
    )
}
