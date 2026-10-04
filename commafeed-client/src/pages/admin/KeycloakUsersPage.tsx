import { Trans } from "@lingui/react/macro"
import { Alert as MantineAlert, Anchor, Container, Table, Text, Title } from "@mantine/core"
import { useAsync } from "react-async-hook"
import { Link as RouterLink } from "react-router-dom"
import { client, errorToStrings } from "@/app/client"
import { Alert } from "@/components/Alert"
import { Loader } from "@/components/Loader"
import { RelativeDate } from "@/components/RelativeDate"

export function KeycloakUsersPage() {
    const query = useAsync(async () => await client.admin.getKeycloakUsers(), [])

    if (!query.result && !query.error) return <Loader />
    if (query.error) {
        const messages = errorToStrings(query.error)
        return (
            <Container>
                <Alert messages={messages.length > 0 ? messages : ["Keycloak user directory is unavailable."]} />
            </Container>
        )
    }

    const users = query.result?.data ?? []
    return (
        <Container>
            <Title order={3} mb="md">
                <Trans>Keycloak user directory</Trans>
            </Title>
            {users.length === 0 ? (
                <MantineAlert>
                    <Text>
                        <Trans>No Keycloak users found.</Trans>
                    </Text>
                </MantineAlert>
            ) : (
                <Table striped highlightOnHover>
                    <Table.Thead>
                        <Table.Tr>
                            <Table.Th>
                                <Trans>Username</Trans>
                            </Table.Th>
                            <Table.Th>
                                <Trans>E-mail</Trans>
                            </Table.Th>
                            <Table.Th>
                                <Trans>Name</Trans>
                            </Table.Th>
                            <Table.Th>
                                <Trans>Enabled</Trans>
                            </Table.Th>
                            <Table.Th>
                                <Trans>Created</Trans>
                            </Table.Th>
                        </Table.Tr>
                    </Table.Thead>
                    <Table.Tbody>
                        {users.map(user => (
                            <Table.Tr key={user.id}>
                                <Table.Td>
                                    <Anchor component={RouterLink} to={`/app/admin/keycloak/users/${encodeURIComponent(user.id)}`}>
                                        {user.username}
                                    </Anchor>
                                </Table.Td>
                                <Table.Td>{user.email ?? ""}</Table.Td>
                                <Table.Td>{[user.firstName, user.lastName].filter(Boolean).join(" ")}</Table.Td>
                                <Table.Td>{user.enabled ? <Trans>Yes</Trans> : <Trans>No</Trans>}</Table.Td>
                                <Table.Td>
                                    <RelativeDate date={user.createdTimestamp} />
                                </Table.Td>
                            </Table.Tr>
                        ))}
                    </Table.Tbody>
                </Table>
            )}
        </Container>
    )
}
