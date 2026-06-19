import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Button, Checkbox, Container, Group, Table, Text, Title } from "@mantine/core"
import { openConfirmModal } from "@mantine/modals"
import { useState } from "react"
import { useAsync, useAsyncCallback } from "react-async-hook"
import { TbArchiveOff, TbTrash } from "react-icons/tb"
import { client, errorToStrings } from "@/app/client"
import { useAppDispatch } from "@/app/store"
import { reloadTree } from "@/app/tree/thunks"
import { Alert } from "@/components/Alert"
import { Loader } from "@/components/Loader"
import { RelativeDate } from "@/components/RelativeDate"

export function ArchivedPage() {
    const dispatch = useAppDispatch()
    const query = useAsync(async () => await client.feed.getArchived(), [])
    const feeds = query.result?.data
    const [selected, setSelected] = useState<number[]>([])

    const onActionSuccess = () => {
        setSelected([])
        query.execute()
        dispatch(reloadTree())
    }
    const unarchive = useAsyncCallback(async (ids: number[]) => await Promise.all(ids.map(id => client.feed.unarchive({ id }))), {
        onSuccess: onActionSuccess,
    })
    const unsubscribe = useAsyncCallback(async (ids: number[]) => await Promise.all(ids.map(id => client.feed.unsubscribe({ id }))), {
        onSuccess: onActionSuccess,
    })

    const toggle = (id: number, checked: boolean) => setSelected(current => (checked ? [...current, id] : current.filter(i => i !== id)))
    const toggleAll = (checked: boolean) => setSelected(checked && feeds ? feeds.map(f => f.id) : [])

    const openUnarchiveModal = () =>
        openConfirmModal({
            title: <Trans>Unarchive</Trans>,
            children: (
                <Text size="sm">
                    <Trans>Are you sure you want to unarchive the selected feeds?</Trans>
                </Text>
            ),
            labels: { confirm: <Trans>Confirm</Trans>, cancel: <Trans>Cancel</Trans> },
            onConfirm: () => unarchive.execute(selected),
        })

    const openUnsubscribeModal = () =>
        openConfirmModal({
            title: <Trans>Unsubscribe</Trans>,
            children: (
                <Text size="sm">
                    <Trans>Are you sure you want to permanently unsubscribe from the selected feeds?</Trans>
                </Text>
            ),
            labels: { confirm: <Trans>Confirm</Trans>, cancel: <Trans>Cancel</Trans> },
            confirmProps: { color: "red" },
            onConfirm: () => unsubscribe.execute(selected),
        })

    if (!feeds) return <Loader />
    return (
        <Container>
            <Title order={3} mb="md">
                <Trans>Archived feeds</Trans>
            </Title>

            {unarchive.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(unarchive.error)} />
                </Box>
            )}
            {unsubscribe.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(unsubscribe.error)} />
                </Box>
            )}

            {feeds.length === 0 ? (
                <Text c="dimmed">
                    <Trans>You have no archived feeds.</Trans>
                </Text>
            ) : (
                <>
                    <Group mb="md">
                        <Button
                            variant="default"
                            leftSection={<TbArchiveOff size={16} />}
                            disabled={selected.length === 0}
                            loading={unarchive.loading}
                            onClick={openUnarchiveModal}
                        >
                            <Trans>Unarchive</Trans>
                        </Button>
                        <Button
                            color="red"
                            leftSection={<TbTrash size={16} />}
                            disabled={selected.length === 0}
                            loading={unsubscribe.loading}
                            onClick={openUnsubscribeModal}
                        >
                            <Trans>Unsubscribe</Trans>
                        </Button>
                    </Group>

                    <Table striped highlightOnHover>
                        <Table.Thead>
                            <Table.Tr>
                                <Table.Th w={40}>
                                    <Checkbox
                                        aria-label="Select all"
                                        checked={selected.length === feeds.length}
                                        indeterminate={selected.length > 0 && selected.length < feeds.length}
                                        onChange={e => toggleAll(e.currentTarget.checked)}
                                    />
                                </Table.Th>
                                <Table.Th>
                                    <Trans>Name</Trans>
                                </Table.Th>
                                <Table.Th>
                                    <Trans>Archived</Trans>
                                </Table.Th>
                                <Table.Th>
                                    <Trans>Last update</Trans>
                                </Table.Th>
                                <Table.Th>
                                    <Trans>Category</Trans>
                                </Table.Th>
                            </Table.Tr>
                        </Table.Thead>
                        <Table.Tbody>
                            {feeds.map(f => (
                                <Table.Tr key={f.id}>
                                    <Table.Td>
                                        <Checkbox
                                            aria-label={f.name}
                                            checked={selected.includes(f.id)}
                                            onChange={e => toggle(f.id, e.currentTarget.checked)}
                                        />
                                    </Table.Td>
                                    <Table.Td>
                                        <Anchor href={f.feedUrl} target="_blank" rel="noreferrer">
                                            {f.name}
                                        </Anchor>
                                    </Table.Td>
                                    <Table.Td>
                                        <RelativeDate date={f.archivedDate} />
                                    </Table.Td>
                                    <Table.Td>
                                        <RelativeDate date={f.lastRefresh} />
                                    </Table.Td>
                                    <Table.Td>{f.categoryName ?? <Trans>Uncategorized</Trans>}</Table.Td>
                                </Table.Tr>
                            ))}
                        </Table.Tbody>
                    </Table>
                </>
            )}
        </Container>
    )
}
