import { Center, Container, RingProgress, Text, useMantineTheme } from "@mantine/core"
import { useAppLoading } from "hooks/useAppLoading"
import { PageTitle } from "./PageTitle"

export function LoadingPage() {
    const theme = useMantineTheme()
    const { loadingPercentage, loadingStepLabel } = useAppLoading()

    return (
        <Container className={"cf-LoadingPage-Container"} size="xs">
            <PageTitle />

            <Center className={"cf-LoadingPage-Container-Center"}>
                <RingProgress
                    className={"cf-LoadingPage-Container-Center-RingProgress"}
                    sections={[{ value: loadingPercentage, color: theme.primaryColor }]}
                    label={
                        <Text className={"cf-LoadingPage-Container-Center-RingProgress-Text"} fw="bold" ta="center" size="xl">
                            {loadingPercentage}%
                        </Text>
                    }
                />
            </Center>

            {loadingStepLabel && <Center className={"cf-LoadingPage-Container-StepLabel"}>{loadingStepLabel}</Center>}
        </Container>
    )
}
