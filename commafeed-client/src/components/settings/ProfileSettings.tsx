import { t, Trans } from "@lingui/macro"
import { Anchor, Box, Button, Checkbox, Divider, Group, Input, PasswordInput, Stack, Text, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { openConfirmModal } from "@mantine/modals"
import { client, errorToStrings } from "app/client"
import { redirectToLogin, redirectToSelectedSource } from "app/slices/redirect"
import { reloadProfile } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { ProfileModificationRequest } from "app/types"
import { Alert } from "components/Alert"
import { useEffect } from "react"
import { useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy, TbTrash } from "react-icons/tb"

interface FormData extends ProfileModificationRequest {
    newPasswordConfirmation?: string
}

export function ProfileSettings() {
    const profile = useAppSelector(state => state.user.profile)
    const dispatch = useAppDispatch()

    const form = useForm<FormData>({
        validate: {
            newPasswordConfirmation: (value, values) => (value !== values.newPassword ? t`Passwords do not match` : null),
        },
    })
    const { setValues } = form

    const saveProfile = useAsyncCallback(client.user.saveProfile, {
        onSuccess: () => {
            dispatch(reloadProfile())
            dispatch(redirectToSelectedSource())
        },
    })
    const deleteProfile = useAsyncCallback(client.user.deleteProfile, {
        onSuccess: () => {
            dispatch(redirectToLogin())
        },
    })

    const openDeleteProfileModal = () =>
        openConfirmModal({
            title: <Trans>Delete account</Trans>,
            children: (
                <Text size="sm">
                    <Trans>Are you sure you want to delete your account? There's no turning back!</Trans>
                </Text>
            ),
            labels: { confirm: <Trans>Confirm</Trans>, cancel: <Trans>Cancel</Trans> },
            confirmProps: { color: "red" },
            onConfirm: () => deleteProfile.execute(),
        })

    useEffect(() => {
        if (!profile) return
        setValues({
            currentPassword: "",
            email: profile.email ?? "",
            newApiKey: false,
        })
    }, [setValues, profile])

    return (
        <>
            {saveProfile.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(saveProfile.error)} />
                </Box>
            )}

            {deleteProfile.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(deleteProfile.error)} />
                </Box>
            )}

            <form onSubmit={form.onSubmit(saveProfile.execute)}>
                <Stack>
                    <TextInput label={<Trans>User name</Trans>} readOnly value={profile?.name} />
                    <TextInput label={<Trans>API key</Trans>} readOnly value={profile?.apiKey} />

                    <Input.Wrapper
                        label={<Trans>OPML export</Trans>}
                        description={
                            <Trans>
                                Export your subscriptions and categories as an OPML file that can be imported in other feed reading services
                            </Trans>
                        }
                    >
                        <Box>
                            <Anchor href="rest/feed/export" download="commafeed_opml.xml">
                                <Trans>Download</Trans>
                            </Anchor>
                        </Box>
                    </Input.Wrapper>

                    <Input.Wrapper
                        label={<Trans>Fever API</Trans>}
                        description={
                            <Trans>
                                CommaFeed is compatible with the Fever API. Use the following URL in your Fever-compatible mobile client.
                                The username is your user name and the password is your API key.
                            </Trans>
                        }
                    >
                        <Box>
                            <Anchor href={`rest/fever/user/${profile?.id}`} target="_blank">
                                <Trans>Fever API URL</Trans>
                            </Anchor>
                        </Box>
                    </Input.Wrapper>

                    <Divider />

                    <PasswordInput
                        label={<Trans>Current password</Trans>}
                        description={<Trans>Enter your current password to change profile settings</Trans>}
                        required
                        {...form.getInputProps("currentPassword")}
                    />
                    <TextInput type="email" label={<Trans>E-mail</Trans>} {...form.getInputProps("email")} required />
                    <PasswordInput
                        label={<Trans>New password</Trans>}
                        description={<Trans>Changing password will generate a new API key</Trans>}
                        {...form.getInputProps("newPassword")}
                    />
                    <PasswordInput label={<Trans>Confirm password</Trans>} {...form.getInputProps("newPasswordConfirmation")} />
                    <Checkbox label={<Trans>Generate new API key</Trans>} {...form.getInputProps("newApiKey", { type: "checkbox" })} />

                    <Group>
                        <Button variant="default" onClick={() => dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftIcon={<TbDeviceFloppy size={16} />} loading={saveProfile.loading}>
                            <Trans>Save</Trans>
                        </Button>
                        <Divider orientation="vertical" />
                        <Button
                            color="red"
                            leftIcon={<TbTrash size={16} />}
                            onClick={() => openDeleteProfileModal()}
                            loading={deleteProfile.loading}
                        >
                            <Trans>Delete account</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
