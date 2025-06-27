import { Accordion, Box } from "@mantine/core"
import { useEffect } from "react"
import { useAsync } from "react-async-hook"
import { client } from "@/app/client"
import { Loader } from "@/components/Loader"
import { Gauge } from "@/components/metrics/Gauge"
import { Meter } from "@/components/metrics/Meter"
import { MetricAccordionItem } from "@/components/metrics/MetricAccordionItem"

const shownMeters: Record<string, string> = {
    "com.commafeed.backend.feed.FeedRefreshEngine.refill": "Feed queue refill rate",
    "com.commafeed.backend.feed.FeedRefreshWorker.feedFetched": "Feed fetching rate",
    "com.commafeed.backend.feed.FeedRefreshUpdater.feedUpdated": "Feed update rate",
    "com.commafeed.backend.feed.FeedRefreshUpdater.entryInserted": "Entries inserted",
    "com.commafeed.backend.service.db.DatabaseCleaningService.entriesDeleted": "Entries deleted",
}

const shownGauges: Record<string, string> = {
    "com.commafeed.backend.feed.FeedRefreshEngine.queue.size": "Feed Refresh Engine queue size",
    "com.commafeed.backend.feed.FeedRefreshEngine.worker.active": "Feed Refresh Engine active HTTP workers",
    "com.commafeed.backend.feed.FeedRefreshEngine.updater.active": "Feed Refresh Engine active database update workers",
    "com.commafeed.backend.HttpGetter.pool.max": "HttpGetter max pool size",
    "com.commafeed.backend.HttpGetter.pool.size": "HttpGetter current pool size",
    "com.commafeed.backend.HttpGetter.pool.leased": "HttpGetter active connections",
    "com.commafeed.backend.HttpGetter.pool.pending": "HttpGetter waiting for a connection",
    "com.commafeed.backend.HttpGetter.cache.size": "HttpGetter cached entries",
    "com.commafeed.backend.HttpGetter.cache.memoryUsage": "HttpGetter cache memory usage",
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
    const { meters, gauges } = query.result.data
    return (
        <>
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
        </>
    )
}
