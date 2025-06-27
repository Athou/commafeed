import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Kbd, Stack, Table } from "@mantine/core"
import { useOs } from "@mantine/hooks"
import { Constants } from "@/app/constants"

export function KeyboardShortcutsHelp() {
    const isMacOS = useOs() === "macos"
    return (
        <Stack gap="xs">
            <Table striped highlightOnHover>
                <Table.Tbody>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Refresh</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>R</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Open next entry</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>J</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Open previous entry</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>K</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Select next unread feed/category</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>Shift</Kbd>
                            <span> + </span>
                            <Kbd>J</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Select previous unread feed/category</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>Shift</Kbd>
                            <span> + </span>
                            <Kbd>K</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Set focus on next entry without opening it</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>N</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Set focus on previous entry without opening it</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>P</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Move the page down</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>
                                <Trans>Space</Trans>
                            </Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Move the page up</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>
                                <Trans>Shift</Trans>
                            </Kbd>
                            <span> + </span>
                            <Kbd>
                                <Trans>Space</Trans>
                            </Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Open/close current entry</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>O</Kbd>
                            <span>, </span>
                            <Kbd>
                                <Trans>Enter</Trans>
                            </Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Open current entry in a new tab</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>V</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Open current entry in a new tab in the background</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>B</Kbd>
                            <span>*, </span>
                            <Kbd>
                                <Trans>Middle click</Trans>
                            </Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Toggle read status of current entry</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>M</Kbd>
                            <span>, </span>
                            <Trans>Swipe header to the left</Trans>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Toggle starred status of current entry</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>S</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Mark all entries as read</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>
                                <Trans>Shift</Trans>
                            </Kbd>
                            <span> + </span>
                            <Kbd>A</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Go to the All view</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>G</Kbd>
                            <span> </span>
                            <Kbd>A</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Navigate to a subscription by entering its name</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>{isMacOS ? <Trans>Cmd</Trans> : <Trans>Ctrl</Trans>}</Kbd>
                            <span> + </span>
                            <Kbd>K</Kbd>
                            <span>, </span>
                            <Kbd>G</Kbd>
                            <span> </span>
                            <Kbd>U</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Show entry menu (desktop)</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>
                                <Trans>Right click</Trans>
                            </Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Show native menu (desktop)</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>
                                <Trans>Shift</Trans>
                            </Kbd>
                            <span> + </span>
                            <Kbd>
                                <Trans>Right click</Trans>
                            </Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Show entry menu (mobile)</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>
                                <Trans>Long press</Trans>
                            </Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Toggle sidebar</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>F</Kbd>
                        </Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Td>
                            <Trans>Show keyboard shortcut help</Trans>
                        </Table.Td>
                        <Table.Td>
                            <Kbd>?</Kbd>
                        </Table.Td>
                    </Table.Tr>
                </Table.Tbody>
            </Table>
            <Box>
                <span>* </span>
                <Anchor href={Constants.browserExtensionUrl} target="_blank" rel="noreferrer">
                    <Trans>Browser extension required for Chrome</Trans>
                </Anchor>
            </Box>
        </Stack>
    )
}
