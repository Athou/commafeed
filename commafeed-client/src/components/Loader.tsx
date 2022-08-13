import { Center, Loader as MantineLoader } from "@mantine/core"

export function Loader() {
    return (
        <Center>
            <MantineLoader size="xl" variant="bars" />
        </Center>
    )
}
