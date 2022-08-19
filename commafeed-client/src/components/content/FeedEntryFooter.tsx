import { t } from "@lingui/macro"
import { Checkbox, Group, Popover } from "@mantine/core"
import { markEntry, starEntry } from "app/slices/entries"
import { useAppDispatch, useAppSelector } from "app/store"
import { Entry } from "app/types"
import { ActionButton } from "components/ActionButtton"
import { TbExternalLink, TbShare, TbStar, TbStarOff } from "react-icons/tb"
import { ShareButtons } from "./ShareButtons"

interface FeedEntryFooterProps {
    entry: Entry
}

export function FeedEntryFooter(props: FeedEntryFooterProps) {
    const sharingSettings = useAppSelector(state => state.user.settings?.sharingSettings)
    const dispatch = useAppDispatch()

    const showSharingButtons =
        sharingSettings && (Object.values(sharingSettings) as Array<typeof sharingSettings[keyof typeof sharingSettings]>).some(v => v)

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

            {showSharingButtons && (
                <Popover withArrow withinPortal shadow="md">
                    <Popover.Target>
                        <ActionButton icon={<TbShare size={18} />} label={t`Share`} />
                    </Popover.Target>
                    <Popover.Dropdown>
                        <ShareButtons url={props.entry.url} description={props.entry.title} />
                    </Popover.Dropdown>
                </Popover>
            )}

            <a href={props.entry.url} target="_blank" rel="noreferrer">
                <ActionButton icon={<TbExternalLink size={18} />} label={t`Open link`} />
            </a>
        </Group>
    )
}
