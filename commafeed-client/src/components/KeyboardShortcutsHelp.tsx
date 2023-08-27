import { Trans } from "@lingui/macro"
import { Anchor, Box, Kbd, Stack, Table } from "@mantine/core"
import { Constants } from "app/constants"

export function KeyboardShortcutsHelp() {
    return (
        <Stack spacing="xs">
            <Table striped highlightOnHover>
                <tbody>
                    <tr>
                        <td>
                            <Trans>Refresh</Trans>
                        </td>
                        <td>
                            <Kbd>R</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Open next entry</Trans>
                        </td>
                        <td>
                            <Kbd>J</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Open previous entry</Trans>
                        </td>
                        <td>
                            <Kbd>K</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Set focus on next entry without opening it</Trans>
                        </td>
                        <td>
                            <Kbd>N</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Set focus on previous entry without opening it</Trans>
                        </td>
                        <td>
                            <Kbd>P</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Move the page down</Trans>
                        </td>
                        <td>
                            <Kbd>
                                <Trans>Space</Trans>
                            </Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Move the page up</Trans>
                        </td>
                        <td>
                            <Kbd>
                                <Trans>Shift</Trans>
                            </Kbd>
                            <span> + </span>
                            <Kbd>
                                <Trans>Space</Trans>
                            </Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Open/close current entry</Trans>
                        </td>
                        <td>
                            <Kbd>O</Kbd>
                            <span>, </span>
                            <Kbd>
                                <Trans>Enter</Trans>
                            </Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Open current entry in a new tab</Trans>
                        </td>
                        <td>
                            <Kbd>V</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Open current entry in a new tab in the background</Trans>
                        </td>
                        <td>
                            <Kbd>B</Kbd>
                            <span>*, </span>
                            <Kbd>
                                <Trans>Middle click</Trans>
                            </Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Toggle read status of current entry</Trans>
                        </td>
                        <td>
                            <Kbd>M</Kbd>
                            <span>, </span>
                            <Trans>Swipe header to the right</Trans>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Toggle starred status of current entry</Trans>
                        </td>
                        <td>
                            <Kbd>S</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Mark all entries as read</Trans>
                        </td>
                        <td>
                            <Kbd>
                                <Trans>Shift</Trans>
                            </Kbd>
                            <span> + </span>
                            <Kbd>A</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Go to the All view</Trans>
                        </td>
                        <td>
                            <Kbd>G</Kbd>
                            <span> </span>
                            <Kbd>A</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Navigate to a subscription by entering its name</Trans>
                        </td>
                        <td>
                            <Kbd>
                                <Trans>Ctrl</Trans>
                            </Kbd>
                            <span> + </span>
                            <Kbd>K</Kbd>
                            <span>, </span>
                            <Kbd>G</Kbd>
                            <span> </span>
                            <Kbd>U</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Show entry menu (desktop)</Trans>
                        </td>
                        <td>
                            <Kbd>
                                <Trans>Right click</Trans>
                            </Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Show native menu (desktop)</Trans>
                        </td>
                        <td>
                            <Kbd>
                                <Trans>Shift</Trans>
                            </Kbd>
                            <span> + </span>
                            <Kbd>
                                <Trans>Right click</Trans>
                            </Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Show entry menu (mobile)</Trans>
                        </td>
                        <td>
                            <Kbd>
                                <Trans>Long press</Trans>
                            </Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Toggle sidebar</Trans>
                        </td>
                        <td>
                            <Kbd>F</Kbd>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <Trans>Show keyboard shortcut help</Trans>
                        </td>
                        <td>
                            <Kbd>?</Kbd>
                        </td>
                    </tr>
                </tbody>
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
