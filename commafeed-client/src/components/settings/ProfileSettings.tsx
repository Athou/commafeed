import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Button, Checkbox, Divider, Group, Input, PasswordInput, Stack, Text, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { openConfirmModal } from "@mantine/modals"
import { useEffect } from "react"
import { useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy, TbTrash } from "react-icons/tb"
import { client, errorToStrings } from "@/app/client"
import { redirectToLogin, redirectToSelectedSource } from "@/app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { ProfileModificationRequest } from "@/app/types"
import { reloadProfile } from "@/app/user/thunks"
import { Alert } from "@/components/Alert"
import { useValidationRules } from "@/hooks/useValidationRules"

interface FormData extends ProfileModificationRequest {
    newPasswordConfirmation?: string
}

export function ProfileSettings() {
    const profile = useAppSelector(state => state.user.profile)
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const dispatch = useAppDispatch()
    const { _ } = useLingui()
    const validationRules = useValidationRules()

    const form = useForm<FormData>({
        validate: {
            newPassword: validationRules.password,
            newPasswordConfirmation: (value, values) => validationRules.passwordConfirmation(value, values.newPassword),
        },
        validateInputOnChange: true,
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
            onConfirm: () => {
                deleteProfile.execute()
            },
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
                    <TextInput
                        label={<Trans>API key</Trans>}
                        description={
                            <Trans>
                                This is your API key. It can be used for some read-only API operations and grants access to the Fever API.
                                Use the form at the bottom of the page to generate a new API key
                            </Trans>
                        }
                        readOnly
                        value={profile?.apiKey}
                    />

                    <Input.Wrapper
                        label={<Trans>OPML export</Trans>}
                        description={
                            <Trans>
                                Export your subscriptions and categories as an OPML file that can be imported in other feed reading services
                            </Trans>
                        }
                    >
                        <Box>
                            <Anchor href="rest/feed/export" download="commafeed.opml">
                                <Trans>Download</Trans>
                            </Anchor>
                        </Box>
                    </Input.Wrapper>

                    <Input.Wrapper
                        label={<Trans>Fever API</Trans>}
                        description={
                            <Trans>
                                CommaFeed is compatible with the Fever API. Use the following URL in your Fever-compatible mobile client.
                                Login with your username and your <u>API key</u>.
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
                    <TextInput
                        type="email"
                        label={<Trans>E-mail</Trans>}
                        {...form.getInputProps("email")}
                        required={serverInfos?.emailAddressRequired}
                    />
                    <PasswordInput
                        label={<Trans>New password</Trans>}
                        description={<Trans>Changing password will generate a new API key</Trans>}
                        {...form.getInputProps("newPassword")}
                    />
                    <PasswordInput label={<Trans>Confirm password</Trans>} {...form.getInputProps("newPasswordConfirmation")} />
                    <Checkbox label={<Trans>Generate new API key</Trans>} {...form.getInputProps("newApiKey", { type: "checkbox" })} />

                    <Group>
                        <Button variant="default" onClick={async () => await dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftSection={<TbDeviceFloppy size={16} />} loading={saveProfile.loading}>
                            <Trans>Save</Trans>
                        </Button>
                        <Divider orientation="vertical" />
                        <Button
                            color="red"
                            leftSection={<TbTrash size={16} />}
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
