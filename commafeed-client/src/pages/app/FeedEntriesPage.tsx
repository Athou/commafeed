import { Trans } from "@lingui/macro"
import { ActionIcon, Box, Center, createStyles, Divider, Group, Title, useMantineTheme } from "@mantine/core"
import { useViewportSize } from "@mantine/hooks"
import { Constants } from "app/constants"
import { EntrySourceType, loadEntries } from "app/slices/entries"
import { redirectToCategoryDetails, redirectToFeedDetails, redirectToTagDetails } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { flattenCategoryTree } from "app/utils"
import { FeedEntries } from "components/content/FeedEntries"
import { useEffect } from "react"
import { TbEdit } from "react-icons/tb"
import { useLocation, useParams } from "react-router-dom"

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

const useStyles = createStyles(() => ({
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

    const titleClicked = () => {
        if (props.sourceType === "category") dispatch(redirectToCategoryDetails(id))
        else if (props.sourceType === "feed") dispatch(redirectToFeedDetails(id))
        else if (props.sourceType === "tag") dispatch(redirectToTagDetails(id))
    }

    useEffect(() => {
        dispatch(
            loadEntries({
                source: {
                    type: props.sourceType,
                    id,
                },
                clearSearch: true,
            })
        )
    }, [dispatch, props.sourceType, id, location.state])

    const noSubscriptions = rootCategory && flattenCategoryTree(rootCategory).every(c => c.feeds.length === 0)
    if (noSubscriptions) return <NoSubscriptionHelp />
    return (
        // add some room at the bottom of the page in order to be able to scroll the current entry at the top of the page when expanding
        <Box mb={viewport.height - Constants.layout.headerHeight - 210}>
            <Group spacing="xl">
                {sourceWebsiteUrl && (
                    <a href={sourceWebsiteUrl} target="_blank" rel="noreferrer" className={classes.sourceWebsiteLink}>
                        <Title order={3}>{sourceLabel}</Title>
                    </a>
                )}
                {!sourceWebsiteUrl && <Title order={3}>{sourceLabel}</Title>}
                {sourceLabel && (
                    <ActionIcon onClick={titleClicked} variant="subtle" color={theme.primaryColor}>
                        <TbEdit size={18} />
                    </ActionIcon>
                )}
            </Group>

            <FeedEntries />

            {!hasMore && <Divider my="xl" label={<Trans>No more entries</Trans>} labelPosition="center" />}
        </Box>
    )
}
