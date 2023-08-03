import { Trans } from "@lingui/macro"

import { Button, Code, Group, Modal, Slider, Stack, Text } from "@mantine/core"
import { markAllEntries } from "app/slices/entries"
import { useAppDispatch, useAppSelector } from "app/store"
import { ActionButton } from "components/ActionButton"
import { useState } from "react"
import { TbChecks } from "react-icons/tb"

export function MarkAllAsReadButton(props: { iconSize: number }) {
    const [opened, setOpened] = useState(false)
    const [threshold, setThreshold] = useState(0)
    const source = useAppSelector(state => state.entries.source)
    const sourceLabel = useAppSelector(state => state.entries.sourceLabel)
    const entriesTimestamp = useAppSelector(state => state.entries.timestamp) ?? Date.now()
    const markAllAsReadConfirmation = useAppSelector(state => state.user.settings?.markAllAsReadConfirmation)
    const dispatch = useAppDispatch()

    const buttonClicked = () => {
        if (markAllAsReadConfirmation) {
            setThreshold(0)
            setOpened(true)
        } else {
            dispatch(
                markAllEntries({
                    sourceType: source.type,
                    req: {
                        id: source.id,
                        read: true,
                        olderThan: entriesTimestamp,
                    },
                })
            )
        }
    }

    return (
        <>
            <Modal opened={opened} onClose={() => setOpened(false)} title={<Trans>Mark all entries as read</Trans>}>
                <Stack>
                    <Text size="sm">
                        {threshold === 0 && (
                            <Trans>
                                Are you sure you want to mark all entries of <Code>{sourceLabel}</Code> as read?
                            </Trans>
                        )}
                        {threshold > 0 && (
                            <Trans>
                                Are you sure you want to mark entries older than {threshold} days of <Code>{sourceLabel}</Code> as read?
                            </Trans>
                        )}
                    </Text>
                    <Slider
                        py="xl"
                        min={0}
                        max={28}
                        marks={[
                            { value: 0, label: "0" },
                            { value: 7, label: "7" },
                            { value: 14, label: "14" },
                            { value: 21, label: "21" },
                            { value: 28, label: "28" },
                        ]}
                        value={threshold}
                        onChange={setThreshold}
                    />
                    <Group position="right">
                        <Button variant="default" onClick={() => setOpened(false)}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button
                            color="red"
                            onClick={() => {
                                setOpened(false)
                                dispatch(
                                    markAllEntries({
                                        sourceType: source.type,
                                        req: {
                                            id: source.id,
                                            read: true,
                                            olderThan: entriesTimestamp - threshold * 24 * 60 * 60 * 1000,
                                        },
                                    })
                                )
                            }}
                        >
                            <Trans>Confirm</Trans>
                        </Button>
                    </Group>
                </Stack>
            </Modal>
            <ActionButton icon={<TbChecks size={props.iconSize} />} label={<Trans>Mark all as read</Trans>} onClick={buttonClicked} />
        </>
    )
}
