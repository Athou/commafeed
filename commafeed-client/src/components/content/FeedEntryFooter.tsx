import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Group, Indicator, Popover, TagsInput } from "@mantine/core"
import { TbArrowBarToDown, TbExternalLink, TbMail, TbMailOpened, TbShare, TbStar, TbStarOff, TbTag } from "react-icons/tb"
import { markEntriesUpToEntry, markEntry, starEntry, tagEntry } from "@/app/entries/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { Entry } from "@/app/types"
import { ActionButton } from "@/components/ActionButton"
import { useActionButton } from "@/hooks/useActionButton"
import { useMobile } from "@/hooks/useMobile"
import { ShareButtons } from "./ShareButtons"

interface FeedEntryFooterProps {
    entry: Entry
}

export function FeedEntryFooter(props: Readonly<FeedEntryFooterProps>) {
    const tags = useAppSelector(state => state.user.tags)
    const mobile = useMobile()
    const { spacing } = useActionButton()
    const dispatch = useAppDispatch()
    const { _ } = useLingui()

    const readStatusButtonClicked = async () =>
        await dispatch(
            markEntry({
                entry: props.entry,
                read: !props.entry.read,
            })
        )
    const onTagsChange = async (values: string[]) =>
        await dispatch(
            tagEntry({
                entryId: +props.entry.id,
                tags: values,
            })
        )

    return (
        <Group justify="space-between" className="cf-footer">
            <Group gap={spacing}>
                {props.entry.markable && (
                    <ActionButton
                        icon={props.entry.read ? <TbMail size={18} /> : <TbMailOpened size={18} />}
                        label={props.entry.read ? msg`Keep unread` : msg`Mark as read`}
                        onClick={readStatusButtonClicked}
                    />
                )}
                <ActionButton
                    icon={props.entry.starred ? <TbStarOff size={18} /> : <TbStar size={18} />}
                    label={props.entry.starred ? msg`Unstar` : msg`Star`}
                    onClick={async () =>
                        await dispatch(
                            starEntry({
                                entry: props.entry,
                                starred: !props.entry.starred,
                            })
                        )
                    }
                />

                <Popover withArrow withinPortal shadow="md" closeOnClickOutside={!mobile}>
                    <Popover.Target>
                        <ActionButton icon={<TbShare size={18} />} label={msg`Share`} />
                    </Popover.Target>
                    <Popover.Dropdown>
                        <ShareButtons url={props.entry.url} description={props.entry.title} />
                    </Popover.Dropdown>
                </Popover>

                {tags && (
                    <Popover withArrow shadow="md" closeOnClickOutside={!mobile}>
                        <Popover.Target>
                            <Indicator label={props.entry.tags.length} disabled={props.entry.tags.length === 0} inline size={16}>
                                <ActionButton icon={<TbTag size={18} />} label={msg`Tags`} />
                            </Indicator>
                        </Popover.Target>
                        <Popover.Dropdown>
                            <TagsInput
                                placeholder={_(msg`Tags`)}
                                data={tags}
                                value={props.entry.tags}
                                onChange={onTagsChange}
                                comboboxProps={{
                                    withinPortal: false,
                                }}
                            />
                        </Popover.Dropdown>
                    </Popover>
                )}

                <a href={props.entry.url} target="_blank" rel="noreferrer">
                    <ActionButton icon={<TbExternalLink size={18} />} label={msg`Open link`} />
                </a>
            </Group>

            <ActionButton
                icon={<TbArrowBarToDown size={18} />}
                label={msg`Mark as read up to here`}
                onClick={async () => await dispatch(markEntriesUpToEntry(props.entry))}
            />
        </Group>
    )
}
