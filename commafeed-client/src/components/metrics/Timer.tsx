import { Box } from "@mantine/core"
import type { MetricTimer } from "@/app/types"

interface MetricTimerProps {
    timer: MetricTimer
}

export function Timer(props: Readonly<MetricTimerProps>) {
    return (
        <Box>
            <Box>Mean: {props.timer.mean_rate.toFixed(2)}</Box>
            <Box>Last minute: {props.timer.m1_rate.toFixed(2)}</Box>
            <Box>Last 5 minutes: {props.timer.m5_rate.toFixed(2)}</Box>
            <Box>Last 15 minutes: {props.timer.m15_rate.toFixed(2)}</Box>
            <Box>Units: {props.timer.rate_units}</Box>
            <Box>Total: {props.timer.count}</Box>
        </Box>
    )
}
