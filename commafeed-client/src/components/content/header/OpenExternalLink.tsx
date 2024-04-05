import { Trans } from "@lingui/macro"
import { ActionIcon, Anchor, Tooltip } from "@mantine/core"
import { markEntry } from "app/entries/thunks"
import { useAppDispatch } from "app/store"
import { type Entry } from "app/types"
import { TbExternalLink } from "react-icons/tb"

export function OpenExternalLink(props: { entry: Entry }) {
    const dispatch = useAppDispatch()
    const onClick = (e: React.MouseEvent) => {
        e.stopPropagation()
        dispatch(
            markEntry({
                entry: props.entry,
                read: true,
            })
        )
    }

    return (
        <Anchor href={props.entry.url} target="_blank" rel="noreferrer" onClick={onClick}>
            <Tooltip label={<Trans>Open link</Trans>}>
                <ActionIcon variant="transparent" c="dimmed">
                    <TbExternalLink />
                </ActionIcon>
            </Tooltip>
        </Anchor>
    )
}
