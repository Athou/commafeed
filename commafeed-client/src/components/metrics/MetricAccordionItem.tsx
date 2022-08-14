import { Accordion, Box, Group } from "@mantine/core"

interface MetricAccordionItemProps {
    metricKey: string
    name: string
    headerValue: number
    children: React.ReactNode
}

export function MetricAccordionItem({ metricKey, name, headerValue, children }: MetricAccordionItemProps) {
    return (
        <Accordion.Item value={metricKey} key={metricKey}>
            <Accordion.Control>
                <Group position="apart">
                    <Box>{name}</Box>
                    <Box>{headerValue}</Box>
                </Group>
            </Accordion.Control>
            <Accordion.Panel>{children}</Accordion.Panel>
        </Accordion.Item>
    )
}
