import { Trans } from "@lingui/macro"
import { Tooltip } from "@mantine/core"
import dayjs from "dayjs"
import { useEffect, useState } from "react"

export function RelativeDate(props: { date: Date | number | undefined }) {
    const [now, setNow] = useState(new Date())
    useEffect(() => {
        const interval = setInterval(() => setNow(new Date()), 60 * 1000)
        return () => clearInterval(interval)
    }, [])

    if (!props.date) return <Trans>N/A</Trans>
    const date = dayjs(props.date)
    return (
        <Tooltip label={date.toDate().toLocaleString()} openDelay={500}>
            <span>{date.from(dayjs(now))}</span>
        </Tooltip>
    )
}
