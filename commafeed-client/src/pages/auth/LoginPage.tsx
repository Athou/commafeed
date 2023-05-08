import { t, Trans } from "@lingui/macro"
import { Anchor, Box, Button, Center, Container, Group, Paper, PasswordInput, Stack, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { redirectToRootCategory } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { LoginRequest } from "app/types"
import { Alert } from "components/Alert"
import { PageTitle } from "pages/PageTitle"
import { useAsyncCallback } from "react-async-hook"
import { Link } from "react-router-dom"

export function LoginPage() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const dispatch = useAppDispatch()

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
                            placeholder={t`User Name or E-mail`}
                            {...form.getInputProps("name")}
                            description={
                                serverInfos?.demoAccountEnabled ? <Trans>Try out CommaFeed with the demo account: demo/demo</Trans> : ""
                            }
                            size="md"
                            required
                        />
                        <PasswordInput
                            label={<Trans>Password</Trans>}
                            placeholder={t`Password`}
                            {...form.getInputProps("password")}
                            size="md"
                            required
                        />

                        {serverInfos?.smtpEnabled && (
                            <Anchor component={Link} to="/passwordRecovery" color="dimmed">
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
