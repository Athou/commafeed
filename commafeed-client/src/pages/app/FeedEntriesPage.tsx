import { Trans } from "@lingui/react/macro"
import { ActionIcon, Box, Center, Divider, Group, Title, useMantineTheme } from "@mantine/core"
import { useViewportSize } from "@mantine/hooks"
import { Constants } from "app/constants"
import type { EntrySourceType } from "app/entries/slice"
import { loadEntries } from "app/entries/thunks"
import { redirectToCategoryDetails, redirectToFeedDetails, redirectToTagDetails } from "app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import { flattenCategoryTree } from "app/utils"
import { FeedEntries } from "components/content/FeedEntries"
import { useEffect } from "react"
import { TbEdit } from "react-icons/tb"
import { useLocation, useParams } from "react-router-dom"
import { tss } from "tss"

function NoSubscriptionHelp() {
    return (
        <Box>
            <Center>
                <Trans>
                    You don't have any subscriptions yet. Why not try adding one by clicking on the + sign at the top of the page?
                </Trans>
            </Center>
        </Box>
    )
}

interface FeedEntriesPageProps {
    sourceType: EntrySourceType
}

const useStyles = tss.create(() => ({
    sourceWebsiteLink: {
        color: "inherit",
        textDecoration: "none",
    },
}))

export function FeedEntriesPage(props: FeedEntriesPageProps) {
    const { classes } = useStyles()
    const location = useLocation()
    const { id = Constants.categories.all.id } = useParams()
    const viewport = useViewportSize()
    const theme = useMantineTheme()
    const rootCategory = useAppSelector(state => state.tree.rootCategory)
    const sourceLabel = useAppSelector(state => state.entries.sourceLabel)
    const sourceWebsiteUrl = useAppSelector(state => state.entries.sourceWebsiteUrl)
    const hasMore = useAppSelector(state => state.entries.hasMore)
    const dispatch = useAppDispatch()

    let title: React.ReactNode = sourceLabel
    if (id === Constants.categories.all.id) {
        title = <Trans>All</Trans>
    } else if (id === Constants.categories.starred.id) {
        title = <Trans>Starred</Trans>
    }

    const titleClicked = () => {
        switch (props.sourceType) {
            case "category":
                dispatch(redirectToCategoryDetails(id))
                break
            case "feed":
                dispatch(redirectToFeedDetails(id))
                break
            case "tag":
                dispatch(redirectToTagDetails(id))
                break
        }
    }

    // biome-ignore lint/correctness/useExhaustiveDependencies: we subscribe to state.timestamp because we want to reload entries even if the props are the same
    useEffect(() => {
        const promise = dispatch(
            loadEntries({
                source: {
                    type: props.sourceType,
                    id,
                },
                clearSearch: true,
            })
        )
        return () => promise.abort()
    }, [dispatch, props.sourceType, id, location.state?.timestamp])

    const noSubscriptions = rootCategory && flattenCategoryTree(rootCategory).every(c => c.feeds.length === 0)
    if (noSubscriptions) return <NoSubscriptionHelp />
    return (
        // add some room at the bottom of the page in order to be able to scroll the current entry at the top of the page when expanding
        <Box mb={viewport.height * 0.7}>
            <Group gap="xl" className="cf-entries-title">
                {sourceWebsiteUrl && (
                    <a href={sourceWebsiteUrl} target="_blank" rel="noreferrer" className={classes.sourceWebsiteLink}>
                        <Title order={3}>{title}</Title>
                    </a>
                )}
                {!sourceWebsiteUrl && <Title order={3}>{title}</Title>}
                <ActionIcon onClick={titleClicked} variant="subtle" color={theme.primaryColor}>
                    <TbEdit size={18} />
                </ActionIcon>
            </Group>

            <FeedEntries />

            {!hasMore && <Divider my="xl" label={<Trans>No more entries</Trans>} labelPosition="center" />}
        </Box>
    )
}
