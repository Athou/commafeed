import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Button, Center, Container, Group, Paper, PasswordInput, Stack, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useAsyncCallback } from "react-async-hook"
import { Link } from "react-router-dom"
import { client, errorToStrings } from "@/app/client"
import { redirectToRootCategory } from "@/app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { LoginRequest } from "@/app/types"
import { Alert } from "@/components/Alert"
import { PageTitle } from "@/pages/PageTitle"

export function LoginPage() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const dispatch = useAppDispatch()
    const { _ } = useLingui()

    const form = useForm<LoginRequest>({
        initialValues: {
            name: "",
            password: "",
        },
    })

    const login = useAsyncCallback(client.user.login, {
        onSuccess: () => {
            dispatch(redirectToRootCategory())
        },
    })

    return (
        <Container size="xs">
            <PageTitle />
            <Paper>
                <Title order={2} mb="md">
                    <Trans>Log in</Trans>
                </Title>
                {login.error && (
                    <Box mb="md">
                        <Alert messages={errorToStrings(login.error)} />
                    </Box>
                )}
                <form onSubmit={form.onSubmit(login.execute)}>
                    <Stack>
                        <TextInput
                            label={<Trans>User Name or E-mail</Trans>}
                            placeholder={_(msg`User Name or E-mail`)}
                            {...form.getInputProps("name")}
                            description={
                                serverInfos?.demoAccountEnabled ? <Trans>Try out CommaFeed with the demo account: demo/demo</Trans> : ""
                            }
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

                        {serverInfos?.smtpEnabled && (
                            <Anchor component={Link} to="/passwordRecovery" c="dimmed">
                                <Trans>Forgot password?</Trans>
                            </Anchor>
                        )}

                        <Button type="submit" loading={login.loading}>
                            <Trans>Log in</Trans>
                        </Button>
                        {serverInfos?.allowRegistrations && (
                            <Center>
                                <Group>
                                    <Trans>
                                        <Box>Need an account?</Box>
                                        <Anchor component={Link} to="/register">
                                            Sign up!
                                        </Anchor>
                                    </Trans>
                                </Group>
                            </Center>
                        )}
                    </Stack>
                </form>
            </Paper>
        </Container>
    )
}
