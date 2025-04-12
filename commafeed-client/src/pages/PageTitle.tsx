import { Center, Title } from "@mantine/core"
import { Logo } from "components/Logo"

export function PageTitle() {
    return (
        <Center className={"cf-PageTitle-Center"} my="xl">
            <Logo size={48} />
            <Title className={"cf-PageTitle-Center-Title"} order={1} ml="md">
                CommaFeed
            </Title>
        </Center>
    )
}
