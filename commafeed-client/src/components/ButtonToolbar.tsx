import { Group } from "@mantine/core"

export function ButtonToolbar(props: { children: React.ReactNode }) {
    return <Group spacing={14}>{props.children}</Group>
}
