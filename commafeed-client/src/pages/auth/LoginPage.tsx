import { t, Trans } from "@lingui/macro"
import { Anchor, Box, Button, Center, Container, Group, Paper, PasswordInput, Stack, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { redirectToRootCategory } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { LoginRequest } from "app/types"
import { Alert } from "components/Alert"
import { Logo } from "components/Logo"
import { Link } from "react-router-dom"
import useMutation from "use-mutation"

export function LoginPage() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const dispatch = useAppDispatch()

    const form = useForm<LoginRequest>({
        initialValues: {
            name: "",
            password: "",
        },
    })

    const [login, loginResult] = useMutation(client.user.login, {
        onSuccess: () => {
            dispatch(redirectToRootCategory())
        },
    })
    const errors = errorToStrings(loginResult.error)

    return (
        <Container size="xs">
            <Center my="xl">
                <Logo size={48} />
                <Title order={1} ml="md">
                    CommaFeed
                </Title>
            </Center>
            <Paper>
                <Title order={2} mb="md">
                    <Trans>Log in</Trans>
                </Title>
                {errors.length > 0 && (
                    <Box mb="md">
                        <Alert messages={errors} />
                    </Box>
                )}
                <form onSubmit={form.onSubmit(login)}>
                    <Stack>
                        <TextInput
                            label={t`User Name or E-mail`}
                            placeholder={t`User Name or E-mail`}
                            {...form.getInputProps("name")}
                            size="md"
                            required
                        />
                        <PasswordInput
                            label={t`Password`}
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

                        <Button type="submit" loading={loginResult.status === "running"}>
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
