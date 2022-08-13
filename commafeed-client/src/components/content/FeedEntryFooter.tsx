import { t } from "@lingui/macro"
import { Checkbox, Group } from "@mantine/core"
import { markEntry } from "app/slices/entries"
import { useAppDispatch } from "app/store"
import { Entry } from "app/types"
import { ActionButton } from "components/ActionButtton"
import { TbExternalLink } from "react-icons/tb"

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
            <a href={props.entry.url} target="_blank" rel="noreferrer">
                <ActionButton icon={<TbExternalLink size={18} />} label={t`Open link`} />
            </a>
        </Group>
    )
}
