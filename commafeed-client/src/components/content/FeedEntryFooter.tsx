import { t } from "@lingui/macro"
import { Checkbox, Group, MultiSelect, Popover } from "@mantine/core"
import { Constants } from "app/constants"
import { markEntriesUpToEntry, markEntry, starEntry, tagEntry } from "app/slices/entries"
import { useAppDispatch, useAppSelector } from "app/store"
import { Entry } from "app/types"
import { ActionButton } from "components/ActionButtton"
import { useEffect, useState } from "react"
import { TbArrowBarToDown, TbExternalLink, TbShare, TbStar, TbStarOff, TbTag } from "react-icons/tb"
import { ShareButtons } from "./ShareButtons"

interface FeedEntryFooterProps {
    entry: Entry
}

export function FeedEntryFooter(props: FeedEntryFooterProps) {
    const [scrollPosition, setScrollPosition] = useState(0)
    const sharingSettings = useAppSelector(state => state.user.settings?.sharingSettings)
    const tags = useAppSelector(state => state.user.tags)
    const dispatch = useAppDispatch()

    const showSharingButtons =
        sharingSettings && (Object.values(sharingSettings) as Array<typeof sharingSettings[keyof typeof sharingSettings]>).some(v => v)

    const readStatusCheckboxClicked = () => dispatch(markEntry({ entry: props.entry, read: !props.entry.read }))
    const onTagsChange = (values: string[]) =>
        dispatch(
            tagEntry({
                entryId: +props.entry.id,
                tags: values,
            })
        )

    useEffect(() => {
        const scrollArea = document.getElementById(Constants.dom.mainScrollAreaId)

        const listener = () => setScrollPosition(scrollArea ? scrollArea.scrollTop : 0)
        scrollArea?.addEventListener("scroll", listener)
        return () => scrollArea?.removeEventListener("scroll", listener)
    }, [])

    return (
        <Group position="apart">
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
                    <Popover withArrow withinPortal shadow="md" positionDependencies={[scrollPosition]}>
                        <Popover.Target>
                            <ActionButton icon={<TbShare size={18} />} label={t`Share`} />
                        </Popover.Target>
                        <Popover.Dropdown>
                            <ShareButtons url={props.entry.url} description={props.entry.title} />
                        </Popover.Dropdown>
                    </Popover>
                )}

                {tags && (
                    <Popover withArrow withinPortal shadow="md" positionDependencies={[scrollPosition]}>
                        <Popover.Target>
                            <ActionButton icon={<TbTag size={18} />} label={t`Tags`} />
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
                    <ActionButton icon={<TbExternalLink size={18} />} label={t`Open link`} />
                </a>
            </Group>

            <ActionButton
                icon={<TbArrowBarToDown size={18} />}
                label={t`Mark as read up to here`}
                onClick={() => dispatch(markEntriesUpToEntry(props.entry))}
            />
        </Group>
    )
}
