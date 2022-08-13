import { Center, Container, RingProgress, Text, Title, useMantineTheme } from "@mantine/core"
import { Logo } from "components/Logo"
import { useAppLoading } from "hooks/useAppLoading"

export function LoadingPage() {
    const theme = useMantineTheme()
    const { loadingPercentage, loadingStepLabel } = useAppLoading()

    return (
        <Container size="xs">
            <Center my="xl">
                <Logo size={48} />
                <Title order={1} ml="md">
                    CommaFeed
                </Title>
            </Center>

            <Center>
                <RingProgress
                    sections={[{ value: loadingPercentage, color: theme.primaryColor }]}
                    label={
                        <Text weight="bold" align="center" size="xl">
                            {loadingPercentage}%
                        </Text>
                    }
                />
            </Center>

            {loadingStepLabel && <Center>{loadingStepLabel}</Center>}
        </Container>
    )
}
