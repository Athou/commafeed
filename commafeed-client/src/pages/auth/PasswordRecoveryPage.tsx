import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Button, Center, Container, Group, Paper, Stack, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useState } from "react"
import { useAsyncCallback } from "react-async-hook"
import { Link } from "react-router-dom"
import { client, errorToStrings } from "@/app/client"
import type { PasswordResetRequest } from "@/app/types"
import { Alert } from "@/components/Alert"
import { PageTitle } from "@/pages/PageTitle"

export function PasswordRecoveryPage() {
    const [message, setMessage] = useState("")
    const { _ } = useLingui()

    const form = useForm<PasswordResetRequest>({
        initialValues: {
            email: "",
        },
    })

    const recoverPassword = useAsyncCallback(client.user.passwordReset, {
        onSuccess: () => {
            setMessage(_(msg`An email has been sent if this address was registered. Check your inbox.`))
        },
    })

    return (
        <Container size="xs">
            <PageTitle />
            <Paper>
                <Title order={2} mb="md">
                    <Trans>Password Recovery</Trans>
                </Title>

                {recoverPassword.error && (
                    <Box mb="md">
                        <Alert messages={errorToStrings(recoverPassword.error)} />
                    </Box>
                )}

                {message && (
                    <Box mb="md">
                        <Alert level="success" messages={[message]} />
                    </Box>
                )}

                <form
                    onSubmit={form.onSubmit(req => {
                        setMessage("")
                        recoverPassword.execute(req)
                    })}
                >
                    <Stack>
                        <TextInput
                            type="email"
                            label={<Trans>E-mail</Trans>}
                            placeholder={_(msg`E-mail`)}
                            {...form.getInputProps("email")}
                            size="md"
                            required
                        />

                        <Button type="submit" loading={recoverPassword.loading}>
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
