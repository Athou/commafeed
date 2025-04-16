import { Trans } from "@lingui/react/macro"

import { Anchor, Box, Button, Container, Group, Input, Stack, Title } from "@mantine/core"
import { Constants } from "app/constants"
import { redirectToSelectedSource } from "app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import { useParams } from "react-router-dom"

export function TagDetailsPage() {
    const { id = Constants.categories.all.id } = useParams()

    const apiKey = useAppSelector(state => state.user.profile?.apiKey)
    const dispatch = useAppDispatch()

    return (
        <Container className="cf-TagDetails-Container">
            <Stack className="cf-TagDetails-Container-Stack">
                <Title className="cf-TagDetails-Container-Stack-Title" order={3}>
                    {id}
                </Title>
                <Input.Wrapper className="cf-TagDetails-Container-Stack-InputWrapper" label={<Trans>Generated feed url</Trans>}>
                    <Box className="cf-TagDetails-Container-Stack-InputWrapper-Box">
                        {apiKey && (
                            <Anchor
                                className="cf-TagDetails-Container-Stack-InputWrapper-Box-Anchor"
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

                <Group className="cf-TagDetails-Container-Stack-Group">
                    <Button
                        className="cf-TagDetails-Container-Stack-Group-Button"
                        variant="default"
                        onClick={async () => await dispatch(redirectToSelectedSource())}
                    >
                        <Trans>Cancel</Trans>
                    </Button>
                </Group>
            </Stack>
        </Container>
    )
}
