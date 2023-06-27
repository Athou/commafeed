import { t, Trans } from "@lingui/macro"
import { Group, Indicator, MultiSelect, Popover } from "@mantine/core"
import { markEntriesUpToEntry, markEntry, starEntry, tagEntry } from "app/slices/entries"
import { useAppDispatch, useAppSelector } from "app/store"
import { Entry } from "app/types"
import { ActionButton } from "components/ActionButton"
import { useActionButton } from "hooks/useActionButton"
import { useMobile } from "hooks/useMobile"
import { TbArrowBarToDown, TbExternalLink, TbEyeCheck, TbEyeOff, TbShare, TbStar, TbStarOff, TbTag } from "react-icons/tb"
import { ShareButtons } from "./ShareButtons"

interface FeedEntryFooterProps {
    entry: Entry
}

export function FeedEntryFooter(props: FeedEntryFooterProps) {
    const sharingSettings = useAppSelector(state => state.user.settings?.sharingSettings)
    const tags = useAppSelector(state => state.user.tags)
    const mobile = useMobile()
    const { spacing } = useActionButton()
    const dispatch = useAppDispatch()

    const showSharingButtons = sharingSettings && Object.values(sharingSettings).some(v => v)

    const readStatusButtonClicked = () => dispatch(markEntry({ entry: props.entry, read: !props.entry.read }))
    const onTagsChange = (values: string[]) =>
        dispatch(
            tagEntry({
                entryId: +props.entry.id,
                tags: values,
            })
        )

    return (
        <Group position="apart">
            <Group spacing={spacing}>
                {props.entry.markable && (
                    <ActionButton
                        icon={props.entry.read ? <TbEyeOff size={18} /> : <TbEyeCheck size={18} />}
                        label={props.entry.read ? <Trans>Keep unread</Trans> : <Trans>Mark as read</Trans>}
                        onClick={readStatusButtonClicked}
                    />
                )}
                <ActionButton
                    icon={props.entry.starred ? <TbStarOff size={18} /> : <TbStar size={18} />}
                    label={props.entry.starred ? <Trans>Unstar</Trans> : <Trans>Star</Trans>}
                    onClick={() => dispatch(starEntry({ entry: props.entry, starred: !props.entry.starred }))}
                />

                {showSharingButtons && (
                    <Popover withArrow withinPortal shadow="md" closeOnClickOutside={!mobile}>
                        <Popover.Target>
                            <ActionButton icon={<TbShare size={18} />} label={<Trans>Share</Trans>} />
                        </Popover.Target>
                        <Popover.Dropdown>
                            <ShareButtons url={props.entry.url} description={props.entry.title} />
                        </Popover.Dropdown>
                    </Popover>
                )}

                {tags && (
                    <Popover withArrow withinPortal shadow="md" closeOnClickOutside={!mobile}>
                        <Popover.Target>
                            <Indicator label={props.entry.tags.length} disabled={props.entry.tags.length === 0} inline size={16}>
                                <ActionButton icon={<TbTag size={18} />} label={<Trans>Tags</Trans>} />
                            </Indicator>
                        </Popover.Target>
                        <Popover.Dropdown>
                            <MultiSelect
                                data={tags}
                                placeholder="Tags"
                                searchable
                                creatable
                                autoFocus
                                getCreateLabel={query => t`Create tag: ${query}`}
                                value={props.entry.tags}
                                onChange={onTagsChange}
                            />
                        </Popover.Dropdown>
                    </Popover>
                )}

                <a href={props.entry.url} target="_blank" rel="noreferrer">
                    <ActionButton icon={<TbExternalLink size={18} />} label={<Trans>Open link</Trans>} />
                </a>
            </Group>

            <ActionButton
                icon={<TbArrowBarToDown size={18} />}
                label={<Trans>Mark as read up to here</Trans>}
                onClick={() => dispatch(markEntriesUpToEntry(props.entry))}
            />
        </Group>
    )
}
