import { Trans } from "@lingui/react/macro"
import { Button, Code, Group, Modal, Slider, Stack, Text } from "@mantine/core"
import { useState } from "react"
import { Constants } from "@/app/constants"
import { setMarkAllAsReadConfirmationDialogOpen } from "@/app/entries/slice"
import { markAllEntries } from "@/app/entries/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import { selectNextUnreadTreeItem } from "@/app/tree/thunks"

export function MarkAllAsReadConfirmationDialog() {
    const [threshold, setThreshold] = useState(0)
    const open = useAppSelector(state => state.entries.markAllAsReadConfirmationDialogOpen)
    const source = useAppSelector(state => state.entries.source)
    const sourceLabel = useAppSelector(state => state.entries.sourceLabel)
    const entriesTimestamp = useAppSelector(state => state.entries.timestamp) ?? Date.now()
    const markAllAsReadNavigateToNextUnread = useAppSelector(state => state.user.settings?.markAllAsReadNavigateToNextUnread)
    const dispatch = useAppDispatch()

    const onConfirm = async () => {
        dispatch(setMarkAllAsReadConfirmationDialogOpen(false))
        await dispatch(
            markAllEntries({
                sourceType: source.type,
                req: {
                    id: source.id,
                    read: true,
                    olderThan: Date.now() - threshold * 24 * 60 * 60 * 1000,
                    insertedBefore: entriesTimestamp,
                },
            })
        )

        const isAllCategorySelected = source.type === "category" && source.id === Constants.categories.all.id
        if (markAllAsReadNavigateToNextUnread && !isAllCategorySelected) await dispatch(selectNextUnreadTreeItem({ direction: "forward" }))
    }

    return (
        <Modal
            opened={open}
            onClose={() => dispatch(setMarkAllAsReadConfirmationDialogOpen(false))}
            title={<Trans>Mark all entries as read</Trans>}
        >
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
                    data-autofocus
                    onKeyDown={e => e.key === "Enter" && onConfirm()}
                />
                <Group justify="flex-end">
                    <Button variant="default" onClick={() => dispatch(setMarkAllAsReadConfirmationDialogOpen(false))}>
                        <Trans>Cancel</Trans>
                    </Button>
                    <Button color="red" onClick={onConfirm}>
                        <Trans>Confirm</Trans>
                    </Button>
                </Group>
            </Stack>
        </Modal>
    )
}
