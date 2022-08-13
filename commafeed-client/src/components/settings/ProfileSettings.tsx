import { t, Trans } from "@lingui/macro"
import { Anchor, Box, Button, Checkbox, Divider, Group, Input, PasswordInput, Stack, Text, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { openConfirmModal } from "@mantine/modals"
import { client, errorsToStrings } from "app/client"
import { redirectToLogin, redirectToSelectedSource } from "app/slices/redirect"
import { reloadProfile } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { ProfileModificationRequest } from "app/types"
import { Alert } from "components/Alert"
import { useEffect } from "react"
import { TbDeviceFloppy, TbTrash } from "react-icons/tb"
import useMutation from "use-mutation"

interface FormData extends ProfileModificationRequest {
    newPasswordConfirmation?: string
}

export function ProfileSettings() {
    const profile = useAppSelector(state => state.user.profile)
    const dispatch = useAppDispatch()

    const form = useForm<FormData>({
        validate: {
            newPasswordConfirmation: (value: string, values: FormData) => (value !== values.newPassword ? t`Passwords do not match` : null),
        },
    })
    const { setValues } = form

    const [saveProfile, saveProfileResult] = useMutation(client.user.saveProfile, {
        onSuccess: () => {
            dispatch(reloadProfile())
            dispatch(redirectToSelectedSource())
        },
    })
    const [deleteProfile, deleteProfileResult] = useMutation(client.user.deleteProfile, {
        onSuccess: () => {
            dispatch(redirectToLogin())
        },
    })
    const errors = errorsToStrings([saveProfileResult.error, deleteProfileResult.error])

    const openDeleteProfileModal = () =>
        openConfirmModal({
            title: t`Delete account`,
            children: (
                <Text size="sm">
                    <Trans>Are you sure you want to delete your account? There's no turning back!</Trans>
                </Text>
            ),
            labels: { confirm: t`Confirm`, cancel: t`Cancel` },
            confirmProps: { color: "red" },
            onConfirm: () => deleteProfile({}),
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
            {errors.length > 0 && (
                <Box mb="md">
                    <Alert messages={errors} />
                </Box>
            )}

            <form onSubmit={form.onSubmit(saveProfile)}>
                <Stack>
                    <Input.Wrapper label={t`User name`}>
                        <Box>{profile?.name}</Box>
                    </Input.Wrapper>
                    <Input.Wrapper
                        label={t`OPML export`}
                        description={t`Export your subscriptions and categories as an OPML file that can be imported in other feed reading services`}
                    >
                        <Box>
                            <Anchor href="rest/feed/export" download="commafeed_opml.xml">
                                <Trans>Download</Trans>
                            </Anchor>
                        </Box>
                    </Input.Wrapper>
                    <PasswordInput
                        label={t`Current password`}
                        description={t`Enter your current password to change profile settings`}
                        required
                        {...form.getInputProps("currentPassword")}
                    />
                    <TextInput type="email" label={t`E-mail`} {...form.getInputProps("email")} required />
                    <PasswordInput
                        label={t`New password`}
                        description={t`Changing password will generate a new API key`}
                        {...form.getInputProps("newPassword")}
                    />
                    <PasswordInput label={t`Confirm password`} {...form.getInputProps("newPasswordConfirmation")} />
                    <TextInput label={t`API key`} readOnly value={profile?.apiKey} />
                    <Checkbox label={t`Generate new API key`} {...form.getInputProps("newApiKey", { type: "checkbox" })} />

                    <Group>
                        <Button variant="default" onClick={() => dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftIcon={<TbDeviceFloppy size={16} />} loading={saveProfileResult.status === "running"}>
                            <Trans>Save</Trans>
                        </Button>
                        <Divider orientation="vertical" />
                        <Button
                            color="red"
                            leftIcon={<TbTrash size={16} />}
                            onClick={() => openDeleteProfileModal()}
                            loading={deleteProfileResult.status === "running"}
                        >
                            <Trans>Delete account</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
