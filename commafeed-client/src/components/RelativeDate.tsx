import { Trans } from "@lingui/react/macro"
import { Tooltip } from "@mantine/core"
import dayjs from "dayjs"
import { Constants } from "@/app/constants"
import { useNow } from "@/hooks/useNow"

export function RelativeDate(props: { date: Date | number | undefined }) {
    const now = useNow(60 * 1000)

    if (!props.date) return <Trans>N/A</Trans>
    const date = dayjs(props.date)
    return (
        <Tooltip label={date.toDate().toLocaleString()} openDelay={Constants.tooltip.delay}>
            <span>{date.from(dayjs(now))}</span>
        </Tooltip>
    )
}
