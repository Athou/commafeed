import { t, Trans } from "@lingui/macro"
import { Anchor, Box, Button, Center, Container, Group, Paper, PasswordInput, Stack, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { redirectToRootCategory } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { RegistrationRequest } from "app/types"
import { Alert } from "components/Alert"
import { Logo } from "components/Logo"
import { Link } from "react-router-dom"
import useMutation from "use-mutation"

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

    const [register, registerResult] = useMutation(client.user.register, {
        onSuccess: () => {
            dispatch(redirectToRootCategory())
        },
    })
    const errors = errorToStrings(registerResult.error)

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
                    <Trans>Sign up</Trans>
                </Title>
                {serverInfos && !serverInfos.allowRegistrations && (
                    <Box mb="md">
                        <Alert messages={[t`Registrations are closed on this CommaFeed instance`]} />
                    </Box>
                )}
                {serverInfos?.allowRegistrations && (
                    <>
                        {errors.length > 0 && (
                            <Box mb="md">
                                <Alert messages={errors} />
                            </Box>
                        )}

                        <form onSubmit={form.onSubmit(register)}>
                            <Stack>
                                <TextInput label="User Name" placeholder="User Name" {...form.getInputProps("name")} size="md" required />
                                <TextInput
                                    type="email"
                                    label={t`E-mail address`}
                                    placeholder={t`E-mail address`}
                                    {...form.getInputProps("email")}
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
                                <Button type="submit" loading={registerResult.status === "running"}>
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
