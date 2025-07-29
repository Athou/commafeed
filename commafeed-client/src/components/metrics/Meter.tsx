import { Box } from "@mantine/core"
import type { MetricMeter } from "@/app/types"

interface MeterProps {
    meter: MetricMeter
}

export function Meter(props: Readonly<MeterProps>) {
    return (
        <Box>
            <Box>Mean: {props.meter.mean_rate.toFixed(2)}</Box>
            <Box>Last minute: {props.meter.m1_rate.toFixed(2)}</Box>
            <Box>Last 5 minutes: {props.meter.m5_rate.toFixed(2)}</Box>
            <Box>Last 15 minutes: {props.meter.m15_rate.toFixed(2)}</Box>
            <Box>Units: {props.meter.units}</Box>
            <Box>Total: {props.meter.count}</Box>
        </Box>
    )
}
