import { Trans } from "@lingui/macro"
import { Box, Button, Checkbox, Group, PasswordInput, Stack, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { UserModel } from "app/types"
import { Alert } from "components/Alert"
import { useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy } from "react-icons/tb"

interface UserEditProps {
    user?: UserModel
    onCancel: () => void
    onSave: () => void
}

export function UserEdit(props: UserEditProps) {
    const form = useForm<UserModel>({
        initialValues: props.user ?? ({ enabled: true } as UserModel),
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

                    <Group>
                        <Button variant="default" onClick={props.onCancel}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftIcon={<TbDeviceFloppy size={16} />} loading={saveUser.loading}>
                            <Trans>Save</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
