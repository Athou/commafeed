import { NumberFormatter } from "@mantine/core"
import type { MetricGauge } from "@/app/types"

interface MeterProps {
    gauge: MetricGauge
}

export function Gauge(props: MeterProps) {
    return <NumberFormatter value={props.gauge.value} thousandSeparator />
}
