import { Trans } from "@lingui/macro"
import { ActionIcon, Box, Code, Container, Group, Table, Text, Title, useMantineTheme } from "@mantine/core"
import { closeAllModals, openConfirmModal, openModal } from "@mantine/modals"
import { client, errorToStrings } from "app/client"
import { UserModel } from "app/types"
import { UserEdit } from "components/admin/UserEdit"
import { Alert } from "components/Alert"
import { Loader } from "components/Loader"
import { RelativeDate } from "components/RelativeDate"
import { ReactNode } from "react"
import { useAsync, useAsyncCallback } from "react-async-hook"
import { TbCheck, TbPencil, TbPlus, TbTrash, TbX } from "react-icons/tb"

function BooleanIcon({ value }: { value: boolean }) {
    return value ? <TbCheck size={18} /> : <TbX size={18} />
}

export function AdminUsersPage() {
    const theme = useMantineTheme()
    const query = useAsync(() => client.admin.getAllUsers(), [])
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
            onConfirm: () => deleteUser.execute({ id: user.id }),
        })
    }

    if (!users) return <Loader />
    return (
        <Container>
            <Title order={3} mb="md">
                <Group>
                    <Trans>Manage users</Trans>
                    <ActionIcon color={theme.primaryColor} onClick={() => openUserEditModal(<Trans>Add user</Trans>)}>
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
                <thead>
                    <tr>
                        <th>
                            <Trans>Id</Trans>
                        </th>
                        <th>
                            <Trans>Name</Trans>
                        </th>
                        <th>
                            <Trans>E-mail</Trans>
                        </th>
                        <th>
                            <Trans>Date created</Trans>
                        </th>
                        <th>
                            <Trans>Last login date</Trans>
                        </th>
                        <th>
                            <Trans>Admin</Trans>
                        </th>
                        <th>
                            <Trans>Enabled</Trans>
                        </th>
                        <th>
                            <Trans>Actions</Trans>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    {users?.map(u => (
                        <tr key={u.id}>
                            <td>{u.id}</td>
                            <td>{u.name}</td>
                            <td>{u.email}</td>
                            <td>
                                <RelativeDate date={u.created} />
                            </td>
                            <td>
                                <RelativeDate date={u.lastLogin} />
                            </td>
                            <td>
                                <BooleanIcon value={u.admin} />
                            </td>
                            <td>
                                <BooleanIcon value={u.enabled} />
                            </td>
                            <td>
                                <Group>
                                    <ActionIcon color={theme.primaryColor} onClick={() => openUserEditModal(<Trans>Edit user</Trans>, u)}>
                                        <TbPencil size={18} />
                                    </ActionIcon>
                                    <ActionIcon
                                        color={theme.primaryColor}
                                        onClick={() => openUserDeleteModal(u)}
                                        loading={deleteUser.loading}
                                    >
                                        <TbTrash size={18} />
                                    </ActionIcon>
                                </Group>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </Table>
        </Container>
    )
}
