import { Trans } from "@lingui/react/macro"
import { ActionIcon, Box, Code, Container, Group, Table, Text, Title, useMantineTheme } from "@mantine/core"
import { closeAllModals, openConfirmModal, openModal } from "@mantine/modals"
import type { ReactNode } from "react"
import { useAsync, useAsyncCallback } from "react-async-hook"
import { TbCheck, TbPencil, TbPlus, TbTrash, TbX } from "react-icons/tb"
import { client, errorToStrings } from "@/app/client"
import type { UserModel } from "@/app/types"
import { Alert } from "@/components/Alert"
import { UserEdit } from "@/components/admin/UserEdit"
import { Loader } from "@/components/Loader"
import { RelativeDate } from "@/components/RelativeDate"

function BooleanIcon({ value }: { value: boolean }) {
    const icon = value ? TbCheck : TbX
    return icon({ size: 18 })
}

export function AdminUsersPage() {
    const theme = useMantineTheme()
    const query = useAsync(async () => await client.admin.getAllUsers(), [])
    const users = query.result?.data.sort((a, b) => a.id - b.id)

    const deleteUser = useAsyncCallback(client.admin.deleteUser, {
        onSuccess: () => {
            query.execute()
            closeAllModals()
        },
    })

    const openUserEditModal = (title: ReactNode, user?: UserModel) => {
        openModal({
            title,
            children: (
                <UserEdit
                    user={user}
                    onCancel={closeAllModals}
                    onSave={() => {
                        query.execute()
                        closeAllModals()
                    }}
                />
            ),
        })
    }

    const openUserDeleteModal = (user: UserModel) => {
        const userName = user.name
        openConfirmModal({
            title: <Trans>Delete user</Trans>,
            children: (
                <Text size="sm">
                    <Trans>
                        Are you sure you want to delete user <Code>{userName}</Code> ?
                    </Trans>
                </Text>
            ),
            labels: { confirm: <Trans>Confirm</Trans>, cancel: <Trans>Cancel</Trans> },
            confirmProps: { color: "red" },
            onConfirm: () => {
                deleteUser.execute({ id: user.id })
            },
        })
    }

    if (!users) return <Loader />
    return (
        <Container>
            <Title order={3} mb="md">
                <Group>
                    <Trans>Manage users</Trans>
                    <ActionIcon color={theme.primaryColor} variant="subtle" onClick={() => openUserEditModal(<Trans>Add user</Trans>)}>
                        <TbPlus size={20} />
                    </ActionIcon>
                </Group>
            </Title>

            {deleteUser.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(deleteUser.error)} />
                </Box>
            )}

            <Table striped highlightOnHover>
                <Table.Thead>
                    <Table.Tr>
                        <Table.Th>
                            <Trans>Id</Trans>
                        </Table.Th>
                        <Table.Th>
                            <Trans>Name</Trans>
                        </Table.Th>
                        <Table.Th>
                            <Trans>E-mail</Trans>
                        </Table.Th>
                        <Table.Th>
                            <Trans>Date created</Trans>
                        </Table.Th>
                        <Table.Th>
                            <Trans>Last login date</Trans>
                        </Table.Th>
                        <Table.Th>
                            <Trans>Admin</Trans>
                        </Table.Th>
                        <Table.Th>
                            <Trans>Enabled</Trans>
                        </Table.Th>
                        <Table.Th>
                            <Trans>Actions</Trans>
                        </Table.Th>
                    </Table.Tr>
                </Table.Thead>
                <Table.Tbody>
                    {users.map(u => (
                        <Table.Tr key={u.id}>
                            <Table.Td>{u.id}</Table.Td>
                            <Table.Td>{u.name}</Table.Td>
                            <Table.Td>{u.email}</Table.Td>
                            <Table.Td>
                                <RelativeDate date={u.created} />
                            </Table.Td>
                            <Table.Td>
                                <RelativeDate date={u.lastLogin} />
                            </Table.Td>
                            <Table.Td>
                                <BooleanIcon value={u.admin} />
                            </Table.Td>
                            <Table.Td>
                                <BooleanIcon value={u.enabled} />
                            </Table.Td>
                            <Table.Td>
                                <Group>
                                    <ActionIcon
                                        color={theme.primaryColor}
                                        variant="subtle"
                                        onClick={() => openUserEditModal(<Trans>Edit user</Trans>, u)}
                                    >
                                        <TbPencil size={18} />
                                    </ActionIcon>
                                    <ActionIcon
                                        color={theme.primaryColor}
                                        variant="subtle"
                                        onClick={() => openUserDeleteModal(u)}
                                        loading={deleteUser.loading}
                                    >
                                        <TbTrash size={18} />
                                    </ActionIcon>
                                </Group>
                            </Table.Td>
                        </Table.Tr>
                    ))}
                </Table.Tbody>
            </Table>
        </Container>
    )
}
