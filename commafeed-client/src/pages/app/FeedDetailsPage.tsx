import { t, Trans } from "@lingui/macro"
import { Anchor, Box, Button, Code, Container, Divider, Group, Input, NumberInput, Stack, Text, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { openConfirmModal } from "@mantine/modals"
import { client, errorsToStrings } from "app/client"
import { redirectToRootCategory, redirectToSelectedSource } from "app/slices/redirect"
import { reloadTree } from "app/slices/tree"
import { useAppDispatch, useAppSelector } from "app/store"
import { FeedModificationRequest } from "app/types"
import { Alert } from "components/Alert"
import { CategorySelect } from "components/content/add/CategorySelect"
import { Loader } from "components/Loader"
import { RelativeDate } from "components/RelativeDate"
import { useEffect } from "react"
import { useAsync } from "react-async-hook"
import { TbDeviceFloppy, TbTrash } from "react-icons/tb"
import { useParams } from "react-router-dom"
import useMutation from "use-mutation"

function FilteringExpressionDescription() {
    const example = <Code>url.contains('youtube') or (author eq 'athou' and title.contains('github')</Code>
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
                    <span>Complete available syntax is available </span>
                    <a href="http://commons.apache.org/proper/commons-jexl/reference/syntax.html" target="_blank" rel="noreferrer">
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
    const query = useAsync(() => client.feed.get(id), [id])
    const feed = query.result?.data

    const form = useForm<FeedModificationRequest>()
    const { setValues } = form

    const [modify, modifyResult] = useMutation(client.feed.modify, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToSelectedSource())
        },
    })
    const [unsubscribe, unsubscribeResult] = useMutation(client.feed.unsubscribe, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToRootCategory())
        },
    })
    const errors = errorsToStrings([modifyResult.error, unsubscribeResult.error])

    const openUnsubscribeModal = () => {
        const feedName = feed?.name
        return openConfirmModal({
            title: t`Unsubscribe`,
            children: (
                <Text size="sm">
                    <Trans>
                        Are you sure you want to unsubscribe from <Code>{feedName}</Code>?
                    </Trans>
                </Text>
            ),
            labels: { confirm: t`Confirm`, cancel: t`Cancel` },
            confirmProps: { color: "red" },
            onConfirm: () => unsubscribe({ id: +id }),
        })
    }

    useEffect(() => {
        if (!feed) return
        setValues(feed)
    }, [setValues, feed])

    if (!feed) return <Loader />
    return (
        <Container>
            {errors.length > 0 && (
                <Box mb="md">
                    <Alert messages={errors} />
                </Box>
            )}

            <form onSubmit={form.onSubmit(modify)}>
                <Stack>
                    <Title order={3}>{feed.name}</Title>
                    <Input.Wrapper label={t`Feed URL`}>
                        <Box>
                            <Anchor href={feed.feedUrl} target="_blank" rel="noreferrer">
                                {feed.feedUrl}
                            </Anchor>
                        </Box>
                    </Input.Wrapper>
                    <Input.Wrapper label={t`Website`}>
                        <Box>
                            <Anchor href={feed.feedLink} target="_blank" rel="noreferrer">
                                {feed.feedLink}
                            </Anchor>
                        </Box>
                    </Input.Wrapper>
                    <Input.Wrapper label={t`Last refresh`}>
                        <Box>
                            <RelativeDate date={feed.lastRefresh} />
                        </Box>
                    </Input.Wrapper>
                    <Input.Wrapper label={t`Last refresh message`}>
                        <Box>{feed.message ?? t`N/A`}</Box>
                    </Input.Wrapper>
                    <Input.Wrapper label={t`Next refresh`}>
                        <Box>
                            <RelativeDate date={feed.nextRefresh} />
                        </Box>
                    </Input.Wrapper>
                    <Input.Wrapper label={t`Generated feed url`}>
                        <Box>
                            {apiKey && (
                                <Anchor href={`rest/feed/entriesAsFeed?id=${feed.id}&apiKey=${apiKey}`} target="_blank" rel="noreferrer">
                                    <Trans>Link</Trans>
                                </Anchor>
                            )}
                            {!apiKey && <Trans>Generate an API key in your profile first.</Trans>}
                        </Box>
                    </Input.Wrapper>

                    <TextInput label={t`Name`} {...form.getInputProps("name")} required />
                    <CategorySelect label={t`Category`} {...form.getInputProps("categoryId")} clearable />
                    <NumberInput label={t`Position`} {...form.getInputProps("position")} required min={0} />
                    <TextInput
                        label={t`Filtering expression`}
                        description={<FilteringExpressionDescription />}
                        {...form.getInputProps("filter")}
                    />

                    <Group>
                        <Button variant="default" onClick={() => dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftIcon={<TbDeviceFloppy size={16} />} loading={modifyResult.status === "running"}>
                            <Trans>Save</Trans>
                        </Button>
                        <Divider orientation="vertical" />
                        <Button
                            color="red"
                            leftIcon={<TbTrash size={16} />}
                            onClick={() => openUnsubscribeModal()}
                            loading={unsubscribeResult.status === "running"}
                        >
                            <Trans>Unsubscribe</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </Container>
    )
}
