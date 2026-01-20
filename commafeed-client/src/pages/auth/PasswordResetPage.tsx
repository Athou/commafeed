import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Button, Center, Container, Group, Paper, PasswordInput, Stack, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useState } from "react"
import { useAsyncCallback } from "react-async-hook"
import { Link, useSearchParams } from "react-router-dom"
import { client, errorToStrings } from "@/app/client"
import { Alert } from "@/components/Alert"
import { useValidationRules } from "@/hooks/useValidationRules"
import { PageTitle } from "@/pages/PageTitle"

interface PasswordResetFormValues {
    password: string
    passwordConfirmation: string
}

export function PasswordResetPage() {
    const [message, setMessage] = useState("")
    const [searchParams] = useSearchParams()
    const { _ } = useLingui()
    const validationRules = useValidationRules()

    const email = searchParams.get("email") ?? ""
    const token = searchParams.get("token") ?? ""

    const form = useForm<PasswordResetFormValues>({
        initialValues: {
            password: "",
            passwordConfirmation: "",
        },
        validate: {
            password: validationRules.password,
            passwordConfirmation: (value, values) => validationRules.passwordConfirmation(value, values.password),
        },
        validateInputOnChange: true,
    })

    const resetPassword = useAsyncCallback(client.user.passwordResetCallback, {
        onSuccess: () => {
            setMessage(_(msg`Your password has been changed. You can now log in with your new password.`))
            form.reset()
        },
    })

    const isMissingParams = !email || !token

    return (
        <Container size="xs">
            <PageTitle />
            <Paper>
                <Title order={2} mb="md">
                    <Trans>Reset Password</Trans>
                </Title>

                {resetPassword.error && (
                    <Box mb="md">
                        <Alert messages={errorToStrings(resetPassword.error)} />
                    </Box>
                )}

                {isMissingParams && (
                    <Box mb="md">
                        <Alert messages={[_(msg`Invalid password reset link. Please request a new one.`)]} />
                    </Box>
                )}

                {message && (
                    <Box mb="md">
                        <Alert level="success" messages={[message]} />
                    </Box>
                )}

                {!isMissingParams && !message && (
                    <form
                        onSubmit={form.onSubmit(values => {
                            resetPassword.execute({
                                email,
                                token,
                                password: values.password,
                            })
                        })}
                    >
                        <Stack>
                            <PasswordInput
                                label={<Trans>New Password</Trans>}
                                placeholder={_(msg`New Password`)}
                                {...form.getInputProps("password")}
                                size="md"
                                required
                            />

                            <PasswordInput
                                label={<Trans>Confirm Password</Trans>}
                                placeholder={_(msg`Confirm Password`)}
                                {...form.getInputProps("passwordConfirmation")}
                                size="md"
                                required
                            />

                            <Button type="submit" loading={resetPassword.loading}>
                                <Trans>Reset Password</Trans>
                            </Button>
                        </Stack>
                    </form>
                )}

                <Center mt="md">
                    <Group>
                        <Anchor component={Link} to="/login">
                            <Trans>Back to log in</Trans>
                        </Anchor>
                    </Group>
                </Center>
            </Paper>
        </Container>
    )
}
