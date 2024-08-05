import { Accordion, Box, Tabs } from "@mantine/core"
import { client } from "app/client"
import { Loader } from "components/Loader"
import { Gauge } from "components/metrics/Gauge"
import { Meter } from "components/metrics/Meter"
import { MetricAccordionItem } from "components/metrics/MetricAccordionItem"
import { Timer } from "components/metrics/Timer"
import { useEffect } from "react"
import { useAsync } from "react-async-hook"
import { TbChartAreaLine, TbClock } from "react-icons/tb"

const shownMeters: Record<string, string> = {
    "com.commafeed.backend.feed.FeedRefreshEngine.refill": "Feed queue refill rate",
    "com.commafeed.backend.feed.FeedRefreshWorker.feedFetched": "Feed fetching rate",
    "com.commafeed.backend.feed.FeedRefreshUpdater.feedUpdated": "Feed update rate",
    "com.commafeed.backend.feed.FeedRefreshUpdater.entryCacheHit": "Entry cache hit rate",
    "com.commafeed.backend.feed.FeedRefreshUpdater.entryCacheMiss": "Entry cache miss rate",
    "com.commafeed.backend.service.db.DatabaseCleaningService.entriesDeleted": "Entries deleted",
}

const shownGauges: Record<string, string> = {
    "com.commafeed.backend.feed.FeedRefreshEngine.queue.size": "Feed Refresh Engine queue size",
    "com.commafeed.backend.feed.FeedRefreshEngine.worker.active": "Feed Refresh Engine active HTTP workers",
    "com.commafeed.backend.feed.FeedRefreshEngine.updater.active": "Feed Refresh Engine active database update workers",
    "com.commafeed.backend.HttpGetter.pool.max": "HttpGetter max connections",
    "com.commafeed.backend.HttpGetter.pool.available": "HttpGetter available connections",
    "com.commafeed.backend.HttpGetter.pool.leased": "HttpGetter leased connections",
    "com.commafeed.backend.HttpGetter.pool.pending": "HttpGetter waiting for available connections",
    "com.commafeed.frontend.ws.WebSocketSessions.users": "WebSocket users",
    "com.commafeed.frontend.ws.WebSocketSessions.sessions": "WebSocket sessions",
}

export function MetricsPage() {
    const query = useAsync(async () => await client.admin.getMetrics(), [], {
        // keep previous results available while a new request is pending
        setLoading: state => ({ ...state, loading: true }),
    })

    useEffect(() => {
        const interval = setInterval(() => query.execute(), 2000)
        return () => clearInterval(interval)
    }, [query.execute])

    if (!query.result) return <Loader />
    const { meters, gauges, timers } = query.result.data
    return (
        <Tabs defaultValue="stats">
            <Tabs.List>
                <Tabs.Tab value="stats" leftSection={<TbChartAreaLine size={14} />}>
                    Stats
                </Tabs.Tab>
                <Tabs.Tab value="timers" leftSection={<TbClock size={14} />}>
                    Timers
                </Tabs.Tab>
            </Tabs.List>

            <Tabs.Panel value="stats" pt="xs">
                <Accordion variant="contained" chevronPosition="left">
                    {Object.keys(shownMeters).map(m => (
                        <MetricAccordionItem key={m} metricKey={m} name={shownMeters[m]} headerValue={meters[m].count}>
                            <Meter meter={meters[m]} />
                        </MetricAccordionItem>
                    ))}
                </Accordion>

                <Box pt="xs">
                    {Object.keys(shownGauges).map(g => (
                        <Box key={g}>
                            <span>{shownGauges[g]}:&nbsp;</span>
                            <Gauge gauge={gauges[g]} />
                        </Box>
                    ))}
                </Box>
            </Tabs.Panel>

            <Tabs.Panel value="timers" pt="xs">
                <Accordion variant="contained" chevronPosition="left">
                    {Object.keys(timers).map(key => (
                        <MetricAccordionItem key={key} metricKey={key} name={key} headerValue={timers[key].count}>
                            <Timer timer={timers[key]} />
                        </MetricAccordionItem>
                    ))}
                </Accordion>
            </Tabs.Panel>
        </Tabs>
    )
}
