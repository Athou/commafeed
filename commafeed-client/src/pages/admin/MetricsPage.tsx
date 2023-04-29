import { Accordion, Tabs } from "@mantine/core"
import { client } from "app/client"
import { Loader } from "components/Loader"
import { Meter } from "components/metrics/Meter"
import { MetricAccordionItem } from "components/metrics/MetricAccordionItem"
import { Timer } from "components/metrics/Timer"
import { useAsync } from "react-async-hook"
import { TbChartAreaLine, TbClock } from "react-icons/tb"

const shownMeters: { [key: string]: string } = {
    "com.commafeed.backend.service.FeedRefreshFlowService.refill": "Feed queue refill rate",
    "com.commafeed.backend.feed.FeedRefreshWorker.feedFetched": "Feed fetching rate",
    "com.commafeed.backend.feed.FeedRefreshUpdater.feedUpdated": "Feed update rate",
    "com.commafeed.backend.feed.FeedRefreshUpdater.entryCacheHit": "Entry cache hit rate",
    "com.commafeed.backend.feed.FeedRefreshUpdater.entryCacheMiss": "Entry cache miss rate",
}

export function MetricsPage() {
    const query = useAsync(() => client.admin.getMetrics(), [])

    if (!query.result) return <Loader />
    const { meters, timers } = query.result.data
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
