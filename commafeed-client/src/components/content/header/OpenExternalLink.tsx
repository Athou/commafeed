import { Trans } from "@lingui/react/macro"
import { ActionIcon, Anchor, Tooltip } from "@mantine/core"
import { TbExternalLink } from "react-icons/tb"
import { Constants } from "@/app/constants"
import { markEntry } from "@/app/entries/thunks"
import { useAppDispatch } from "@/app/store"
import type { Entry } from "@/app/types"

export function OpenExternalLink(
    props: Readonly<{
        entry: Entry
    }>
) {
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
            <Tooltip label={<Trans>Open link</Trans>} openDelay={Constants.tooltip.delay}>
                <ActionIcon variant="transparent" c="dimmed">
                    <TbExternalLink size={18} />
                </ActionIcon>
            </Tooltip>
        </Anchor>
    )
}
