import { t, Trans } from "@lingui/macro"
import { Anchor, Box, Button, Center, Container, Group, Paper, PasswordInput, Stack, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { redirectToRootCategory } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { RegistrationRequest } from "app/types"
import { Alert } from "components/Alert"
import { PageTitle } from "pages/PageTitle"
import { useAsyncCallback } from "react-async-hook"
import { Link } from "react-router-dom"

export function RegistrationPage() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const dispatch = useAppDispatch()

    const form = useForm<RegistrationRequest>({
        initialValues: {
            name: "",
            password: "",
            email: "",
        },
    })

    const register = useAsyncCallback(client.user.register, {
        onSuccess: () => {
            dispatch(redirectToRootCategory())
        },
    })

    return (
        <Container size="xs">
            <PageTitle />
            <Paper>
                <Title order={2} mb="md">
                    <Trans>Sign up</Trans>
                </Title>
                {serverInfos && !serverInfos.allowRegistrations && (
                    <Box mb="md">
                        <Alert messages={[t`Registrations are closed on this CommaFeed instance`]} />
                    </Box>
                )}
                {serverInfos?.allowRegistrations && (
                    <>
                        {register.error && (
                            <Box mb="md">
                                <Alert messages={errorToStrings(register.error)} />
                            </Box>
                        )}

                        <form onSubmit={form.onSubmit(register.execute)}>
                            <Stack>
                                <TextInput label="User Name" placeholder="User Name" {...form.getInputProps("name")} size="md" required />
                                <TextInput
                                    type="email"
                                    label={<Trans>E-mail address</Trans>}
                                    placeholder={t`E-mail address`}
                                    {...form.getInputProps("email")}
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
                                <Button type="submit" loading={register.loading}>
                                    <Trans>Sign up</Trans>
                                </Button>
                                <Center>
                                    <Group>
                                        <Trans>
                                            <Box>Have an account?</Box>
                                            <Anchor component={Link} to="/login">
                                                Log in!
                                            </Anchor>
                                        </Trans>
                                    </Group>
                                </Center>
                            </Stack>
                        </form>
                    </>
                )}
            </Paper>
        </Container>
    )
}
