import { Center, Title } from "@mantine/core"
import { Logo } from "@/components/Logo"

export function PageTitle() {
    return (
        <Center my="xl">
            <Logo size={48} />
            <Title order={1} ml="md">
                CommaFeed
            </Title>
        </Center>
    )
}
