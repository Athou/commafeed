import { Trans } from "@lingui/react/macro"
import { Anchor, Container, Group, Table, Title } from "@mantine/core"
import { useAsync } from "react-async-hook"
import { Link, useParams } from "react-router-dom"
import { client, errorToStrings } from "@/app/client"
import { Alert } from "@/components/Alert"
import { Loader } from "@/components/Loader"
import { RelativeDate } from "@/components/RelativeDate"

export function KeycloakUserDetailsPage() {
    const { id } = useParams()
    const query = useAsync(async () => await client.admin.getKeycloakUser(id ?? ""), [id])

    if (!query.result && !query.error) return <Loader />
    if (query.error) {
        const messages = errorToStrings(query.error)
        return (
            <Container>
                <Alert messages={messages.length > 0 ? messages : ["Keycloak user is unavailable."]} />
            </Container>
        )
    }

    const user = query.result?.data
    if (!user) return <Loader />
    return (
        <Container>
            <Group justify="space-between" mb="md">
                <Title order={3}>
                    <Trans>Keycloak user details</Trans>
                </Title>
                <Anchor component={Link} to="/app/admin/keycloak/users">
                    <Trans>Back to directory</Trans>
                </Anchor>
            </Group>
            <Table withRowBorders>
                <Table.Tbody>
                    <Table.Tr>
                        <Table.Th>
                            <Trans>Id</Trans>
                        </Table.Th>
                        <Table.Td>{user.id}</Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Th>
                            <Trans>Username</Trans>
                        </Table.Th>
                        <Table.Td>{user.username}</Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Th>
                            <Trans>E-mail</Trans>
                        </Table.Th>
                        <Table.Td>{user.email ?? ""}</Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Th>
                            <Trans>First name</Trans>
                        </Table.Th>
                        <Table.Td>{user.firstName ?? ""}</Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Th>
                            <Trans>Last name</Trans>
                        </Table.Th>
                        <Table.Td>{user.lastName ?? ""}</Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Th>
                            <Trans>Enabled</Trans>
                        </Table.Th>
                        <Table.Td>{user.enabled ? <Trans>Yes</Trans> : <Trans>No</Trans>}</Table.Td>
                    </Table.Tr>
                    <Table.Tr>
                        <Table.Th>
                            <Trans>Created</Trans>
                        </Table.Th>
                        <Table.Td>
                            <RelativeDate date={user.createdTimestamp} />
                        </Table.Td>
                    </Table.Tr>
                </Table.Tbody>
            </Table>
        </Container>
    )
}
