import { Center, Container, RingProgress, Text, useMantineTheme } from "@mantine/core"
import { useAppLoading } from "@/hooks/useAppLoading"
import { PageTitle } from "./PageTitle"

export function LoadingPage() {
    const theme = useMantineTheme()
    const { loadingPercentage, loadingStepLabel } = useAppLoading()

    return (
        <Container size="xs">
            <PageTitle />

            <Center>
                <RingProgress
                    sections={[{ value: loadingPercentage, color: theme.primaryColor }]}
                    label={
                        <Text fw="bold" ta="center" size="xl">
                            {loadingPercentage}%
                        </Text>
                    }
                />
            </Center>

            {loadingStepLabel && <Center>{loadingStepLabel}</Center>}
        </Container>
    )
}
