import { Trans } from "@lingui/macro"
import { Button, Group, Stack, Textarea } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client } from "app/client"
import { redirectToSelectedSource } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { useEffect } from "react"
import { useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy } from "react-icons/tb"

interface FormData {
    customCss: string
}

export function CustomCss() {
    const settings = useAppSelector(state => state.user.settings)
    const customCss = settings?.customCss
    const dispatch = useAppDispatch()

    const form = useForm<FormData>()
    const { setValues } = form

    const saveCustomCss = useAsyncCallback(
        async (d: FormData) => {
            if (!settings) return
            await client.user.saveSettings({ ...settings, customCss: d.customCss })
        },
        {
            onSuccess: () => {
                window.location.reload()
            },
        }
    )

    useEffect(() => {
        if (!customCss) return
        setValues({
            customCss,
        })
    }, [setValues, customCss])

    return (
        <form onSubmit={form.onSubmit(saveCustomCss.execute)}>
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
