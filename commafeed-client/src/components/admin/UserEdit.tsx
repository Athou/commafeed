import { Trans } from "@lingui/react/macro"
import { Box, Button, Checkbox, Group, PasswordInput, Stack, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy } from "react-icons/tb"
import { client, errorToStrings } from "@/app/client"
import type { AdminSaveUserRequest, UserModel } from "@/app/types"
import { Alert } from "@/components/Alert"

interface UserEditProps {
    user?: UserModel
    onCancel: () => void
    onSave: () => void
}

export function UserEdit(props: Readonly<UserEditProps>) {
    const form = useForm<AdminSaveUserRequest>({
        initialValues: props.user ?? {
            name: "",
            enabled: true,
            admin: false,
        },
    })
    const saveUser = useAsyncCallback(client.admin.saveUser, { onSuccess: props.onSave })

    return (
        <>
            {saveUser.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(saveUser.error)} />
                </Box>
            )}

            <form onSubmit={form.onSubmit(saveUser.execute)}>
                <Stack>
                    <TextInput label={<Trans>Name</Trans>} {...form.getInputProps("name")} required />
                    <PasswordInput label={<Trans>Password</Trans>} {...form.getInputProps("password")} required={!props.user} />
                    <TextInput type="email" label={<Trans>E-mail</Trans>} {...form.getInputProps("email")} />
                    <Checkbox label={<Trans>Admin</Trans>} {...form.getInputProps("admin", { type: "checkbox" })} />
                    <Checkbox label={<Trans>Enabled</Trans>} {...form.getInputProps("enabled", { type: "checkbox" })} />

                    <Group justify="right">
                        <Button variant="default" onClick={props.onCancel}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftSection={<TbDeviceFloppy size={16} />} loading={saveUser.loading}>
                            <Trans>Save</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
