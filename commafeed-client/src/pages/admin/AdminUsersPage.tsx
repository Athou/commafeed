import { t, Trans } from "@lingui/macro"
import { ActionIcon, Box, Code, Container, Group, Table, Text, Title, useMantineTheme } from "@mantine/core"
import { closeAllModals, openConfirmModal, openModal } from "@mantine/modals"
import { client, errorToStrings } from "app/client"
import { UserModel } from "app/types"
import { UserEdit } from "components/admin/UserEdit"
import { Alert } from "components/Alert"
import { Loader } from "components/Loader"
import { RelativeDate } from "components/RelativeDate"
import { useAsync } from "react-async-hook"
import { TbCheck, TbPencil, TbPlus, TbTrash, TbX } from "react-icons/tb"
import useMutation from "use-mutation"

function BooleanIcon({ value }: { value: boolean }) {
    return value ? <TbCheck size={18} /> : <TbX size={18} />
}

export function AdminUsersPage() {
    const theme = useMantineTheme()
    const query = useAsync(() => client.admin.getAllUsers(), [])
    const users = query.result?.data.sort((a, b) => a.id - b.id)

    const [deleteUser, deleteUserResult] = useMutation(client.admin.deleteUser, {
        onSuccess: () => {
            query.execute()
            closeAllModals()
        },
    })
    const errors = errorToStrings(deleteUserResult.error)

    const openUserEditModal = (title: string, user?: UserModel) => {
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
            title: t`Delete user`,
            children: (
                <Text size="sm">
                    <Trans>
                        Are you sure you want to delete user <Code>{userName}</Code> ?
                    </Trans>
                </Text>
            ),
            labels: { confirm: t`Confirm`, cancel: t`Cancel` },
            confirmProps: { color: "red" },
            onConfirm: () => deleteUser({ id: user.id }),
        })
    }

    if (!users) return <Loader />
    return (
        <Container>
            <Title order={3} mb="md">
                <Group>
                    <Trans>Manage users</Trans>
                    <ActionIcon color={theme.primaryColor} onClick={() => openUserEditModal(t`Add user`)}>
                        <TbPlus size={20} />
                    </ActionIcon>
                </Group>
            </Title>

            {errors.length > 0 && (
                <Box mb="md">
                    <Alert messages={errors} />
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
                                    <ActionIcon color={theme.primaryColor} onClick={() => openUserEditModal(t`Edit user`, u)}>
                                        <TbPencil size={18} />
                                    </ActionIcon>
                                    <ActionIcon
                                        color={theme.primaryColor}
                                        onClick={() => openUserDeleteModal(u)}
                                        loading={deleteUserResult.status === "running"}
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
