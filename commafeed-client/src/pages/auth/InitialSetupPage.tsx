import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Box, Button, Container, Paper, PasswordInput, Stack, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useAsyncCallback } from "react-async-hook"
import { client, errorToStrings } from "@/app/client"
import { redirectToRootCategory } from "@/app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { InitialSetupRequest } from "@/app/types"
import { Alert } from "@/components/Alert"
import { PageTitle } from "@/pages/PageTitle"

export function InitialSetupPage() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const dispatch = useAppDispatch()
    const { _ } = useLingui()

    const form = useForm<InitialSetupRequest>({
        initialValues: {
            name: "",
            password: "",
            email: "",
        },
        validate: {
            password: value =>
                serverInfos && value.length < serverInfos.minimumPasswordLength
                    ? _(msg`Password must be at least ${serverInfos.minimumPasswordLength} characters`)
                    : null,
        },
        validateInputOnChange: true,
    })

    const login = useAsyncCallback(client.user.login, {
        onSuccess: () => {
            dispatch(redirectToRootCategory())
        },
    })

    const setup = useAsyncCallback(client.user.initialSetup, {
        onSuccess: () => {
            login.execute(form.values)
        },
    })

    return (
        <Container size="xs">
            <PageTitle />
            <Paper>
                <Title order={2} mb="md">
                    <Trans>Initial Setup</Trans>
                </Title>
                <Box mb="md">
                    <Trans>
                        Welcome! This appears to be the first time you're running CommaFeed. Please create an administrator account to get
                        started.
                    </Trans>
                </Box>
                {setup.error && (
                    <Box mb="md">
                        <Alert messages={errorToStrings(setup.error)} />
                    </Box>
                )}
                <form onSubmit={form.onSubmit(setup.execute)}>
                    <Stack>
                        <TextInput
                            label={<Trans>Admin user name</Trans>}
                            placeholder={_(msg`Admin user name`)}
                            {...form.getInputProps("name")}
                            size="md"
                            required
                            autoCapitalize="off"
                        />
                        <PasswordInput
                            label={<Trans>Password</Trans>}
                            placeholder={_(msg`Password`)}
                            {...form.getInputProps("password")}
                            size="md"
                            required
                        />
                        <TextInput
                            type="email"
                            label={<Trans>E-mail</Trans>}
                            placeholder={_(msg`E-mail`)}
                            {...form.getInputProps("email")}
                            size="md"
                        />

                        <Button type="submit" loading={setup.loading}>
                            <Trans>Create Admin Account</Trans>
                        </Button>
                    </Stack>
                </form>
            </Paper>
        </Container>
    )
}
