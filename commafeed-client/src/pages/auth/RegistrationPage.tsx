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
import type { RegistrationRequest } from "@/app/types"
import { Alert } from "@/components/Alert"
import { PageTitle } from "@/pages/PageTitle"

export function RegistrationPage() {
    const serverInfos = useAppSelector(state => state.server.serverInfos)
    const dispatch = useAppDispatch()
    const { _ } = useLingui()

    const form = useForm<RegistrationRequest>({
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

    const register = useAsyncCallback(client.user.register, {
        onSuccess: () => {
            login.execute(form.values)
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
                        <Alert messages={[_(msg`Registrations are closed on this CommaFeed instance`)]} />
                    </Box>
                )}
                {serverInfos?.allowRegistrations && (
                    <>
                        {register.error && (
                            <Box mb="md">
                                <Alert messages={errorToStrings(register.error)} />
                            </Box>
                        )}

                        {login.error && (
                            <Box mb="md">
                                <Alert messages={errorToStrings(login.error)} />
                            </Box>
                        )}

                        <form onSubmit={form.onSubmit(register.execute)}>
                            <Stack>
                                <TextInput label="User Name" placeholder="User Name" {...form.getInputProps("name")} size="md" required />
                                <TextInput
                                    type="email"
                                    label={<Trans>E-mail address</Trans>}
                                    placeholder={_(msg`E-mail address`)}
                                    {...form.getInputProps("email")}
                                    size="md"
                                    required={serverInfos.emailAddressRequired}
                                />
                                <PasswordInput
                                    label={<Trans>Password</Trans>}
                                    placeholder={_(msg`Password`)}
                                    {...form.getInputProps("password")}
                                    size="md"
                                    required
                                />
                                <Button type="submit" loading={register.loading || login.loading}>
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
