import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Button, Code, Container, Divider, Group, Input, NumberInput, Stack, Text, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { openConfirmModal } from "@mantine/modals"
import { client, errorToStrings } from "app/client"
import { redirectToRootCategory, redirectToSelectedSource } from "app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import { reloadTree } from "app/tree/thunks"
import type { FeedModificationRequest } from "app/types"
import { Alert } from "components/Alert"
import { Loader } from "components/Loader"
import { RelativeDate } from "components/RelativeDate"
import { CategorySelect } from "components/content/add/CategorySelect"
import { useEffect } from "react"
import { useAsync, useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy, TbTrash } from "react-icons/tb"
import { useParams } from "react-router-dom"

function FilteringExpressionDescription() {
    const example = <Code>url.contains('youtube') or (author eq 'athou' and title.contains('github'))</Code>
    return (
        <div>
            <div>
                <Trans>
                    If not empty, an expression evaluating to 'true' or 'false'. If false, new entries for this feed will be marked as read
                    automatically.
                </Trans>
            </div>
            <div>
                <Trans>
                    Available variables are 'title', 'content', 'url' 'author' and 'categories' and their content is converted to lower case
                    to ease string comparison.
                </Trans>
            </div>
            <div>
                <Trans>Example: {example}.</Trans>
            </div>
            <div>
                <Trans>
                    <span>Complete syntax is available </span>
                    <a href="https://commons.apache.org/proper/commons-jexl/reference/syntax.html" target="_blank" rel="noreferrer">
                        here
                    </a>
                    .
                </Trans>
            </div>
        </div>
    )
}

export function FeedDetailsPage() {
    const { id } = useParams()
    if (!id) throw Error("id required")

    const apiKey = useAppSelector(state => state.user.profile?.apiKey)
    const dispatch = useAppDispatch()
    const query = useAsync(async () => await client.feed.get(id), [id])
    const feed = query.result?.data

    const form = useForm<FeedModificationRequest>()
    const { setValues } = form

    const modifyFeed = useAsyncCallback(client.feed.modify, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToSelectedSource())
        },
    })
    const unsubscribe = useAsyncCallback(client.feed.unsubscribe, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToRootCategory())
        },
    })

    const openUnsubscribeModal = () => {
        const feedName = feed?.name
        openConfirmModal({
            title: <Trans>Unsubscribe</Trans>,
            children: (
                <Text size="sm">
                    <Trans>
                        Are you sure you want to unsubscribe from <Code>{feedName}</Code>?
                    </Trans>
                </Text>
            ),
            labels: { confirm: <Trans>Confirm</Trans>, cancel: <Trans>Cancel</Trans> },
            confirmProps: { color: "red" },
            onConfirm: async () => await unsubscribe.execute({ id: +id }),
        })
    }

    useEffect(() => {
        if (!feed) return
        setValues(feed)
    }, [setValues, feed])

    if (!feed) return <Loader />
    return (
        <Container className="cf-FeedDetails-Container">
            {modifyFeed.error && (
                <Box className="cf-FeedDetails-Container-ModifyFeedError" mb="md">
                    <Alert messages={errorToStrings(modifyFeed.error)} />
                </Box>
            )}

            {unsubscribe.error && (
                <Box className="cf-FeedDetails-Container-UnsubscribeError" mb="md">
                    <Alert messages={errorToStrings(unsubscribe.error)} />
                </Box>
            )}

            <form className="cf-FeedDetails-Container-Form" onSubmit={form.onSubmit(modifyFeed.execute)}>
                <Stack className="cf-FeedDetails-Container-Form-Stack">
                    <Title className="cf-FeedDetails-Container-Form-Stack-Title" order={3}>
                        {feed.name}
                    </Title>
                    <Input.Wrapper className="cf-FeedDetails-Container-Form-Stack-InputWrapper1" label={<Trans>Feed URL</Trans>}>
                        <Box className="cf-FeedDetails-Container-Form-Stack-InputWrapper1-Box">
                            <Anchor
                                className="cf-FeedDetails-Container-Form-Stack-InputWrapper1-Box-Anchor"
                                href={feed.feedUrl}
                                target="_blank"
                                rel="noreferrer"
                            >
                                {feed.feedUrl}
                            </Anchor>
                        </Box>
                    </Input.Wrapper>
                    <Input.Wrapper className="cf-FeedDetails-Container-Form-Stack-InputWrapper2" label={<Trans>Website</Trans>}>
                        <Box className="cf-FeedDetails-Container-Form-Stack-InputWrapper2-Box">
                            <Anchor
                                className="cf-FeedDetails-Container-Form-Stack-InputWrapper2-Box-Anchor"
                                href={feed.feedLink}
                                target="_blank"
                                rel="noreferrer"
                            >
                                {feed.feedLink}
                            </Anchor>
                        </Box>
                    </Input.Wrapper>
                    <Input.Wrapper className="cf-FeedDetails-Container-Form-Stack-InputWrapper3" label={<Trans>Last refresh</Trans>}>
                        <Box className="cf-FeedDetails-Container-Form-Stack-InputWrapper3-Box">
                            <RelativeDate date={feed.lastRefresh} />
                        </Box>
                    </Input.Wrapper>
                    <Input.Wrapper
                        className="cf-FeedDetails-Container-Form-Stack-InputWrapper4"
                        label={<Trans>Last refresh message</Trans>}
                    >
                        <Box className="cf-FeedDetails-Container-Form-Stack-InputWrapper4-Box">{feed.message ?? <Trans>N/A</Trans>}</Box>
                    </Input.Wrapper>
                    <Input.Wrapper className="cf-FeedDetails-Container-Form-Stack-InputWrapper5" label={<Trans>Next refresh</Trans>}>
                        <Box className="cf-FeedDetails-Container-Form-Stack-InputWrapper5-Box">
                            <RelativeDate date={feed.nextRefresh} />
                        </Box>
                    </Input.Wrapper>
                    <Input.Wrapper className="cf-FeedDetails-Container-Form-Stack-InputWrapper6" label={<Trans>Generated feed url</Trans>}>
                        <Box className="cf-FeedDetails-Container-Form-Stack-InputWrapper6-Box">
                            {apiKey && (
                                <Anchor
                                    className="cf-FeedDetails-Container-Form-Stack-InputWrapper6-Box-Anchor"
                                    href={`rest/feed/entriesAsFeed?id=${feed.id}&apiKey=${apiKey}`}
                                    target="_blank"
                                    rel="noreferrer"
                                >
                                    <Trans>Link</Trans>
                                </Anchor>
                            )}
                            {!apiKey && <Trans>Generate an API key in your profile first.</Trans>}
                        </Box>
                    </Input.Wrapper>

                    <Divider className="cf-FeedDetails-Container-Form-Stack-Divider" />

                    <TextInput
                        className="cf-FeedDetails-Container-Form-Stack-TextInput1"
                        label={<Trans>Name</Trans>}
                        {...form.getInputProps("name")}
                        required
                    />
                    <CategorySelect
                        className="cf-FeedDetails-Container-Form-Stack-CategorySelect"
                        label={<Trans>Category</Trans>}
                        {...form.getInputProps("categoryId")}
                        clearable
                    />
                    <NumberInput
                        className="cf-FeedDetails-Container-Form-Stack-NumberInput"
                        label={<Trans>Position</Trans>}
                        {...form.getInputProps("position")}
                        required
                        min={0}
                    />
                    <TextInput
                        className="cf-FeedDetails-Container-Form-Stack-TextInput2"
                        label={<Trans>Filtering expression</Trans>}
                        description={<FilteringExpressionDescription />}
                        {...form.getInputProps("filter")}
                    />

                    <Group className="cf-FeedDetails-Container-Form-Stack-Group">
                        <Button
                            className="cf-FeedDetails-Container-Form-Stack-Group-Button1"
                            variant="default"
                            onClick={async () => await dispatch(redirectToSelectedSource())}
                        >
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button
                            className="cf-FeedDetails-Container-Form-Stack-Group-Button2"
                            type="submit"
                            leftSection={<TbDeviceFloppy size={16} />}
                            loading={modifyFeed.loading}
                        >
                            <Trans>Save</Trans>
                        </Button>
                        <Divider className="cf-FeedDetails-Container-Form-Stack-Group-Divider" orientation="vertical" />
                        <Button
                            className="cf-FeedDetails-Container-Form-Stack-Group-Button3"
                            color="red"
                            leftSection={<TbTrash size={16} />}
                            onClick={() => openUnsubscribeModal()}
                            loading={unsubscribe.loading}
                        >
                            <Trans>Unsubscribe</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </Container>
    )
}
