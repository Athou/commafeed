import { t, Trans } from "@lingui/macro"
import { Anchor, Box, Button, Center, Container, Group, Paper, Stack, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { PasswordResetRequest } from "app/types"
import { Alert } from "components/Alert"
import { Logo } from "components/Logo"
import { useState } from "react"
import { Link } from "react-router-dom"
import useMutation from "use-mutation"

export function PasswordRecoveryPage() {
    const [message, setMessage] = useState("")

    const form = useForm<PasswordResetRequest>({
        initialValues: {
            email: "",
        },
    })

    const [recoverPassword, recoverPasswordResult] = useMutation(client.user.passwordReset, {
        onMutate: () => {
            setMessage("")
        },
        onSuccess: () => {
            setMessage(t`An email has been sent if this address was registered. Check your inbox.`)
        },
    })
    const errors = errorToStrings(recoverPasswordResult.error)

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
                    <Trans>Password Recovery</Trans>
                </Title>
                {errors.length > 0 && (
                    <Box mb="md">
                        <Alert messages={errors} />
                    </Box>
                )}
                {message && (
                    <Box mb="md">
                        <Alert level="success" messages={[message]} />
                    </Box>
                )}
                <form onSubmit={form.onSubmit(recoverPassword)}>
                    <Stack>
                        <TextInput
                            type="email"
                            label={t`E-mail`}
                            placeholder={t`E-mail`}
                            {...form.getInputProps("email")}
                            size="md"
                            required
                        />

                        <Button type="submit" loading={recoverPasswordResult.status === "running"}>
                            <Trans>Recover password</Trans>
                        </Button>

                        <Center>
                            <Group>
                                <Anchor component={Link} to="/login">
                                    <Trans>Back to log in</Trans>
                                </Anchor>
                            </Group>
                        </Center>
                    </Stack>
                </form>
            </Paper>
        </Container>
    )
}
