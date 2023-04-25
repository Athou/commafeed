import { MetricGauge } from "app/types"

interface MeterProps {
    gauge: MetricGauge
}

export function Gauge(props: MeterProps) {
    return <span>{props.gauge.value}</span>
}
