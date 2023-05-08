import { t, Trans } from "@lingui/macro"
import { Box, Center, Kbd, TextInput } from "@mantine/core"
import { openSpotlight, SpotlightAction, SpotlightProvider } from "@mantine/spotlight"
import { redirectToFeed } from "app/slices/redirect"
import { useAppDispatch } from "app/store"
import { Subscription } from "app/types"
import { FeedFavicon } from "components/content/FeedFavicon"
import { useMousetrap } from "hooks/useMousetrap"
import { TbSearch } from "react-icons/tb"

export interface TreeSearchProps {
    feeds: Subscription[]
}

export function TreeSearch(props: TreeSearchProps) {
    const dispatch = useAppDispatch()

    const actions: SpotlightAction[] = props.feeds
        .sort((f1, f2) => f1.name.localeCompare(f2.name))
        .map(f => ({
            title: f.name,
            icon: <FeedFavicon url={f.iconUrl} />,
            onTrigger: () => dispatch(redirectToFeed(f.id)),
        }))

    const searchIcon = <TbSearch size={18} />
    const rightSection = (
        <Center>
            <Kbd>Ctrl</Kbd>
            <Box mx={5}>+</Box>
            <Kbd>K</Kbd>
        </Center>
    )

    // additional keyboard shortcut used by commafeed v1
    useMousetrap("g u", () => openSpotlight())

    return (
        <SpotlightProvider
            actions={actions}
            searchIcon={searchIcon}
            searchPlaceholder={t`Search`}
            shortcut="ctrl+k"
            nothingFoundMessage={<Trans>Nothing found</Trans>}
        >
            <TextInput
                placeholder={t`Search`}
                icon={searchIcon}
                rightSectionWidth={100}
                rightSection={rightSection}
                styles={{
                    input: { cursor: "pointer" },
                    rightSection: { pointerEvents: "none" },
                }}
                onClick={() => openSpotlight()}
                // prevent focus
                onFocus={e => e.target.blur()}
                readOnly
            />
        </SpotlightProvider>
    )
}
