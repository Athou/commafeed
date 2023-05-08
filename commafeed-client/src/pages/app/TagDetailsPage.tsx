import { Trans } from "@lingui/macro"

import { Anchor, Box, Button, Container, Group, Input, Stack, Title } from "@mantine/core"
import { Constants } from "app/constants"
import { redirectToSelectedSource } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { useParams } from "react-router-dom"

export function TagDetailsPage() {
    const { id = Constants.categories.all.id } = useParams()

    const apiKey = useAppSelector(state => state.user.profile?.apiKey)
    const dispatch = useAppDispatch()

    return (
        <Container>
            <Stack>
                <Title order={3}>{id}</Title>
                <Input.Wrapper label={<Trans>Generated feed url</Trans>}>
                    <Box>
                        {apiKey && (
                            <Anchor
                                href={`rest/category/entriesAsFeed?id=${Constants.categories.all.id}&apiKey=${apiKey}&tag=${id}`}
                                target="_blank"
                                rel="noreferrer"
                            >
                                <Trans>Link</Trans>
                            </Anchor>
                        )}
                        {!apiKey && <Trans>Generate an API key in your profile first.</Trans>}
                    </Box>
                </Input.Wrapper>

                <Group>
                    <Button variant="default" onClick={() => dispatch(redirectToSelectedSource())}>
                        <Trans>Cancel</Trans>
                    </Button>
                </Group>
            </Stack>
        </Container>
    )
}
