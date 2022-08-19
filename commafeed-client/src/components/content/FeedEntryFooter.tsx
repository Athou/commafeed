import { t } from "@lingui/macro"
import { Checkbox, Group } from "@mantine/core"
import { markEntry, starEntry } from "app/slices/entries"
import { useAppDispatch } from "app/store"
import { Entry } from "app/types"
import { ActionButton } from "components/ActionButtton"
import { TbExternalLink, TbStar, TbStarOff } from "react-icons/tb"

interface FeedEntryFooterProps {
    entry: Entry
}

export function FeedEntryFooter(props: FeedEntryFooterProps) {
    const dispatch = useAppDispatch()
    const readStatusCheckboxClicked = () => dispatch(markEntry({ entry: props.entry, read: !props.entry.read }))

    return (
        <Group>
            {props.entry.markable && (
                <Checkbox
                    label={t`Keep unread`}
                    checked={!props.entry.read}
                    onChange={readStatusCheckboxClicked}
                    styles={{
                        label: { cursor: "pointer" },
                        input: { cursor: "pointer" },
                    }}
                />
            )}
            <ActionButton
                icon={props.entry.starred ? <TbStarOff size={18} /> : <TbStar size={18} />}
                label={props.entry.starred ? t`Unstar` : t`Star`}
                onClick={() => dispatch(starEntry({ entry: props.entry, starred: !props.entry.starred }))}
            />
            <a href={props.entry.url} target="_blank" rel="noreferrer">
                <ActionButton icon={<TbExternalLink size={18} />} label={t`Open link`} />
            </a>
        </Group>
    )
}
