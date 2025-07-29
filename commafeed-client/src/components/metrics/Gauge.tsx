import { NumberFormatter } from "@mantine/core"
import type { MetricGauge } from "@/app/types"

interface GaugeProps {
    gauge: MetricGauge
}

export function Gauge(props: Readonly<GaugeProps>) {
    return <NumberFormatter value={props.gauge.value} thousandSeparator />
}
