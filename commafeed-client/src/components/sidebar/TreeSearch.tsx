import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { TextInput } from "@mantine/core"
import { Spotlight, type SpotlightActionData, spotlight } from "@mantine/spotlight"
import { redirectToFeed } from "app/redirect/thunks"
import { useAppDispatch } from "app/store"
import type { Subscription } from "app/types"
import { FeedFavicon } from "components/content/FeedFavicon"
import { useMousetrap } from "hooks/useMousetrap"
import { TbSearch } from "react-icons/tb"

export interface TreeSearchProps {
    feeds: Subscription[]
}

export function TreeSearch(props: TreeSearchProps) {
    const dispatch = useAppDispatch()
    const { _ } = useLingui()

    const actions: SpotlightActionData[] = props.feeds
        .map(f => ({
            id: `${f.id}`,
            label: f.name,
            leftSection: <FeedFavicon url={f.iconUrl} />,
            onClick: async () => await dispatch(redirectToFeed(f.id)),
        }))
        .sort((f1, f2) => f1.label.localeCompare(f2.label))

    const searchIcon = <TbSearch size={18} />

    // additional keyboard shortcut used by commafeed v1
    useMousetrap("g u", () => spotlight.open())

    return (
        <>
            <TextInput
                placeholder={_(msg`Search`)}
                leftSection={searchIcon}
                rightSectionWidth={100}
                styles={{
                    input: {
                        cursor: "pointer",
                    },
                }}
                onClick={() => spotlight.open()}
                // prevent focus
                onFocus={e => e.target.blur()}
                readOnly
            />
            <Spotlight
                actions={actions}
                limit={10}
                shortcut="mod+k"
                searchProps={{
                    leftSection: searchIcon,
                    placeholder: _(msg`Search`),
                }}
                nothingFound={<Trans>Nothing found</Trans>}
            />
        </>
    )
}
