import { Trans } from "@lingui/react/macro"
import { ActionIcon, Tooltip } from "@mantine/core"
import { TbStar, TbStarFilled } from "react-icons/tb"
import { Constants } from "@/app/constants"
import { starEntry } from "@/app/entries/thunks"
import { useAppDispatch } from "@/app/store"
import type { Entry } from "@/app/types"

export function Star(props: { entry: Entry }) {
    const dispatch = useAppDispatch()
    const onClick = (e: React.MouseEvent) => {
        e.stopPropagation()
        e.preventDefault()
        dispatch(
            starEntry({
                entry: props.entry,
                starred: !props.entry.starred,
            })
        )
    }

    return (
        <Tooltip label={props.entry.starred ? <Trans>Unstar</Trans> : <Trans>Star</Trans>} openDelay={Constants.tooltip.delay}>
            <ActionIcon variant="transparent" onClick={onClick}>
                {props.entry.starred ? <TbStarFilled size={18} /> : <TbStar size={18} />}
            </ActionIcon>
        </Tooltip>
    )
}
