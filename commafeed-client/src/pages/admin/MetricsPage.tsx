import { Accordion, Box, Tabs } from "@mantine/core"
import { client } from "app/client"
import { Loader } from "components/Loader"
import { Gauge } from "components/metrics/Gauge"
import { Meter } from "components/metrics/Meter"
import { MetricAccordionItem } from "components/metrics/MetricAccordionItem"
import { Timer } from "components/metrics/Timer"
import { useAsync } from "react-async-hook"
import { TbChartAreaLine, TbClock } from "react-icons/tb"

const shownMeters: { [key: string]: string } = {
    "com.commafeed.backend.feed.FeedQueues.refill": "Refresh queue refill rate",
    "com.commafeed.backend.feed.FeedRefreshTaskGiver.feedRefreshed": "Feed refreshed",
    "com.commafeed.backend.feed.FeedRefreshUpdater.feedUpdated": "Feed updated",
    "com.commafeed.backend.feed.FeedRefreshUpdater.entryCacheHit": "Entry cache hit",
    "com.commafeed.backend.feed.FeedRefreshUpdater.entryCacheMiss": "Entry cache miss",
}

const shownGauges: { [key: string]: string } = {
    "com.commafeed.backend.feed.FeedRefreshExecutor.feed-refresh-updater.active": "Feed Updater active",
    "com.commafeed.backend.feed.FeedRefreshExecutor.feed-refresh-updater.pending": "Feed Updater queued",
    "com.commafeed.backend.feed.FeedRefreshExecutor.feed-refresh-worker.active": "Feed Worker active",
    "com.commafeed.backend.feed.FeedRefreshExecutor.feed-refresh-worker.pending": "Feed Worker queued",
    "com.commafeed.backend.feed.FeedQueues.addQueue": "Task Giver Add Queue",
    "com.commafeed.backend.feed.FeedQueues.takeQueue": "Task Giver Take Queue",
    "com.commafeed.backend.feed.FeedQueues.giveBackQueue": "Task Giver Give Back Queue",
}

export function MetricsPage() {
    const query = useAsync(() => client.admin.getMetrics(), [])

    if (!query.result) return <Loader />
    const { meters, gauges, timers } = query.result.data
    return (
        <Tabs defaultValue="stats">
            <Tabs.List>
                <Tabs.Tab value="stats" icon={<TbChartAreaLine size={14} />}>
                    Stats
                </Tabs.Tab>
                <Tabs.Tab value="timers" icon={<TbClock size={14} />}>
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
                            <span>{shownGauges[g]}&nbsp;</span>
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
